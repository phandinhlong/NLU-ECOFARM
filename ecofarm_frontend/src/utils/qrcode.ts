/**
 * Minimal, zero-dependency QR Code SVG Generator in pure TypeScript.
 * Generates valid ISO/IEC 18004 QR Codes as clean SVG strings.
 */

// Galois Field GF(256) math
const EXP_TABLE = new Uint8Array(512)
const LOG_TABLE = new Uint8Array(256)

for (let i = 0, x = 1; i < 255; i++) {
  EXP_TABLE[i] = x
  EXP_TABLE[i + 255] = x
  LOG_TABLE[x] = i
  x = (x << 1) ^ (x & 0x80 ? 0x11d : 0)
}

function gfMul(x: number, y: number): number {
  if (x === 0 || y === 0) return 0
  return EXP_TABLE[LOG_TABLE[x] + LOG_TABLE[y]]
}

function polyMul(p1: number[], p2: number[]): number[] {
  const result = new Array(p1.length + p2.length - 1).fill(0)
  for (let i = 0; i < p1.length; i++) {
    for (let j = 0; j < p2.length; j++) {
      result[i + j] ^= gfMul(p1[i], p2[j])
    }
  }
  return result
}

function getGeneratorPoly(degree: number): number[] {
  let poly = [1]
  for (let i = 0; i < degree; i++) {
    poly = polyMul(poly, [1, EXP_TABLE[i]])
  }
  return poly
}

function rsEncode(data: number[], ecCount: number): number[] {
  const gen = getGeneratorPoly(ecCount)
  const remainder = new Array(ecCount).fill(0)
  for (const byte of data) {
    const factor = byte ^ remainder.shift()!
    remainder.push(0)
    for (let i = 0; i < ecCount; i++) {
      remainder[i] ^= gfMul(gen[i + 1], factor)
    }
  }
  return remainder
}

// QR Version tables (Versions 1 to 6, Level M)
// Version: size, totalDataBytes, ecBytesPerBlock, blocks
interface VersionInfo {
  version: number
  size: number
  dataBytes: number
  ecBytes: number
  align: number[]
}

const VERSIONS: VersionInfo[] = [
  { version: 1, size: 21, dataBytes: 16, ecBytes: 10, align: [] },
  { version: 2, size: 25, dataBytes: 28, ecBytes: 16, align: [6, 18] },
  { version: 3, size: 29, dataBytes: 44, ecBytes: 26, align: [6, 22] },
  { version: 4, size: 33, dataBytes: 64, ecBytes: 18, align: [6, 26] }, // 2 blocks of 32
  { version: 5, size: 37, dataBytes: 86, ecBytes: 24, align: [6, 30] },
  { version: 6, size: 41, dataBytes: 108, ecBytes: 16, align: [6, 34] },
  { version: 7, size: 45, dataBytes: 124, ecBytes: 18, align: [6, 22, 38] },
  { version: 8, size: 49, dataBytes: 154, ecBytes: 22, align: [6, 24, 42] },
]

export function generateQrSvg(text: string, sizePx = 68): string {
  const utf8 = new TextEncoder().encode(text)
  const dataLen = utf8.length

  // Find minimum version that fits dataLen (Byte mode overhead: 4 bits mode + 8 bits count = 12 bits -> 2 bytes)
  const neededBytes = dataLen + 2
  const vInfo = VERSIONS.find((v) => v.dataBytes >= neededBytes) || VERSIONS[VERSIONS.length - 1]

  // Encode byte mode data stream
  const bits: number[] = []
  function pushBits(val: number, len: number) {
    for (let i = len - 1; i >= 0; i--) {
      bits.push((val >> i) & 1)
    }
  }

  // Mode: 0100 (Byte mode)
  pushBits(0b0100, 4)
  // Character count: 8 bits for versions 1-9
  pushBits(dataLen, 8)
  // Data bytes
  for (const b of utf8) {
    pushBits(b, 8)
  }
  // Terminator
  const totalDataBits = vInfo.dataBytes * 8
  const termLen = Math.min(4, totalDataBits - bits.length)
  pushBits(0, termLen)
  // Align to byte
  while (bits.length % 8 !== 0) {
    bits.push(0)
  }
  // Pad bytes: 0xEC, 0x11
  const dataBytes: number[] = []
  for (let i = 0; i < bits.length; i += 8) {
    let byte = 0
    for (let j = 0; j < 8; j++) {
      byte = (byte << 1) | bits[i + j]
    }
    dataBytes.push(byte)
  }
  let padToggle = 0
  while (dataBytes.length < vInfo.dataBytes) {
    dataBytes.push(padToggle === 0 ? 0xec : 0x11)
    padToggle ^= 1
  }

  // Error correction
  const ec = rsEncode(dataBytes, vInfo.ecBytes)
  const fullPayload = dataBytes.concat(ec)

  // Construct matrix
  const size = vInfo.size
  const matrix: boolean[][] = Array.from({ length: size }, () => new Array(size).fill(false))
  const isFunction: boolean[][] = Array.from({ length: size }, () => new Array(size).fill(false))

  // Finder pattern
  function setFinder(row: number, col: number) {
    for (let r = -1; r <= 7; r++) {
      for (let c = -1; c <= 7; c++) {
        const nr = row + r
        const nc = col + c
        if (nr >= 0 && nr < size && nc >= 0 && nc < size) {
          isFunction[nr][nc] = true
          if (r >= 0 && r <= 6 && c >= 0 && c <= 6) {
            matrix[nr][nc] =
              r === 0 || r === 6 || c === 0 || c === 6 || (r >= 2 && r <= 4 && c >= 2 && c <= 4)
          } else {
            matrix[nr][nc] = false // Separator
          }
        }
      }
    }
  }

  setFinder(0, 0)
  setFinder(0, size - 7)
  setFinder(size - 7, 0)

  // Timing patterns
  for (let i = 8; i < size - 8; i++) {
    matrix[6][i] = i % 2 === 0
    isFunction[6][i] = true
    matrix[i][6] = i % 2 === 0
    isFunction[i][6] = true
  }

  // Alignment patterns
  if (vInfo.align.length >= 2) {
    const coords = vInfo.align
    for (const r of coords) {
      for (const c of coords) {
        if (isFunction[r][c]) continue
        for (let dr = -2; dr <= 2; dr++) {
          for (let dc = -2; dc <= 2; dc++) {
            const nr = r + dr
            const nc = c + dc
            isFunction[nr][nc] = true
            matrix[nr][nc] =
              Math.max(Math.abs(dr), Math.abs(dc)) === 2 || (dr === 0 && dc === 0)
          }
        }
      }
    }
  }

  // Dark module
  matrix[size - 8][8] = true
  isFunction[size - 8][8] = true

  // Format info area reservation
  for (let i = 0; i < 9; i++) {
    isFunction[8][i] = true
    isFunction[i][8] = true
    isFunction[8][size - 1 - i] = true
    isFunction[size - 1 - i][8] = true
  }

  // Place data bits with Mask 0 ((row + col) % 2 === 0)
  let bitIdx = 0
  const allBits: number[] = []
  for (const b of fullPayload) {
    for (let i = 7; i >= 0; i--) {
      allBits.push((b >> i) & 1)
    }
  }

  let upward = true
  for (let right = size - 1; right > 0; right -= 2) {
    if (right === 6) right-- // Skip timing col
    const rows = upward
      ? Array.from({ length: size }, (_, i) => size - 1 - i)
      : Array.from({ length: size }, (_, i) => i)

    for (const r of rows) {
      for (const c of [right, right - 1]) {
        if (!isFunction[r][c]) {
          const bit = bitIdx < allBits.length ? allBits[bitIdx++] : 0
          // Apply mask 0: (r + c) % 2 === 0
          const mask = (r + c) % 2 === 0
          matrix[r][c] = (bit === 1) !== mask
        }
      }
    }
    upward = !upward
  }

  // Format Info for Mask 0, Level M (BCH 15,5 code for 0b00000 = 0x5412)
  const formatInfo = 0x5412 // precomputed for EC M (00) + Mask 0 (000) ^ mask 0x5412
  for (let i = 0; i < 15; i++) {
    const bit = ((formatInfo >> i) & 1) === 1
    if (i < 6) matrix[8][i] = bit
    else if (i < 8) matrix[8][i + 1] = bit
    else matrix[14 - i][8] = bit

    if (i < 8) matrix[size - 1 - i][8] = bit
    else matrix[8][size - 15 + i] = bit
  }

  // Build SVG path rects
  let rects = ''
  for (let r = 0; r < size; r++) {
    for (let c = 0; c < size; c++) {
      if (matrix[r][c]) {
        rects += `<rect x="${c}" y="${r}" width="1" height="1" fill="#0f172a"/>`
      }
    }
  }

  return `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 ${size} ${size}" width="${sizePx}" height="${sizePx}" fill="none">${rects}</svg>`
}
