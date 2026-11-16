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
  const logX = LOG_TABLE[x] ?? 0
  const logY = LOG_TABLE[y] ?? 0
  return EXP_TABLE[logX + logY] ?? 0
}

function polyMul(p1: number[], p2: number[]): number[] {
  const result: number[] = new Array<number>(p1.length + p2.length - 1).fill(0)
  for (let i = 0; i < p1.length; i++) {
    const val1 = p1[i] ?? 0
    for (let j = 0; j < p2.length; j++) {
      const val2 = p2[j] ?? 0
      const current = result[i + j] ?? 0
      result[i + j] = current ^ gfMul(val1, val2)
    }
  }
  return result
}

function getGeneratorPoly(degree: number): number[] {
  let poly = [1]
  for (let i = 0; i < degree; i++) {
    const expVal = EXP_TABLE[i] ?? 1
    poly = polyMul(poly, [1, expVal])
  }
  return poly
}

function rsEncode(data: number[], ecCount: number): number[] {
  const gen = getGeneratorPoly(ecCount)
  const remainder: number[] = new Array<number>(ecCount).fill(0)
  for (const byte of data) {
    const firstRem = remainder.shift() ?? 0
    const factor = byte ^ firstRem
    remainder.push(0)
    for (let i = 0; i < ecCount; i++) {
      const genVal = gen[i + 1] ?? 0
      const curRem = remainder[i] ?? 0
      remainder[i] = curRem ^ gfMul(genVal, factor)
    }
  }
  return remainder
}

// QR Version tables (Versions 1 to 8, Level M)
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
  { version: 4, size: 33, dataBytes: 64, ecBytes: 18, align: [6, 26] },
  { version: 5, size: 37, dataBytes: 86, ecBytes: 24, align: [6, 30] },
  { version: 6, size: 41, dataBytes: 108, ecBytes: 16, align: [6, 34] },
  { version: 7, size: 45, dataBytes: 124, ecBytes: 18, align: [6, 22, 38] },
  { version: 8, size: 49, dataBytes: 154, ecBytes: 22, align: [6, 24, 42] },
]

export function generateQrSvg(text: string, sizePx = 68): string {
  const utf8 = new TextEncoder().encode(text)
  const dataLen = utf8.length

  const neededBytes = dataLen + 2
  const fallbackVersion = VERSIONS[VERSIONS.length - 1] as VersionInfo
  const vInfo: VersionInfo = VERSIONS.find((v) => v.dataBytes >= neededBytes) ?? fallbackVersion

  const bits: number[] = []
  function pushBits(val: number, len: number) {
    for (let i = len - 1; i >= 0; i--) {
      bits.push((val >> i) & 1)
    }
  }

  // Byte mode (0100)
  pushBits(0b0100, 4)
  pushBits(dataLen, 8)
  for (const b of utf8) {
    pushBits(b, 8)
  }
  const totalDataBits = vInfo.dataBytes * 8
  const termLen = Math.min(4, totalDataBits - bits.length)
  pushBits(0, termLen)
  while (bits.length % 8 !== 0) {
    bits.push(0)
  }

  const dataBytes: number[] = []
  for (let i = 0; i < bits.length; i += 8) {
    let byte = 0
    for (let j = 0; j < 8; j++) {
      const bitVal = bits[i + j] ?? 0
      byte = (byte << 1) | bitVal
    }
    dataBytes.push(byte)
  }
  let padToggle = 0
  while (dataBytes.length < vInfo.dataBytes) {
    dataBytes.push(padToggle === 0 ? 0xec : 0x11)
    padToggle ^= 1
  }

  const ec = rsEncode(dataBytes, vInfo.ecBytes)
  const fullPayload = dataBytes.concat(ec)

  const size = vInfo.size
  const matrix: boolean[][] = Array.from({ length: size }, () => new Array<boolean>(size).fill(false))
  const isFunction: boolean[][] = Array.from({ length: size }, () => new Array<boolean>(size).fill(false))

  function setFinder(row: number, col: number) {
    for (let r = -1; r <= 7; r++) {
      for (let c = -1; c <= 7; c++) {
        const nr = row + r
        const nc = col + c
        if (nr >= 0 && nr < size && nc >= 0 && nc < size) {
          const fnRow = isFunction[nr]
          const matRow = matrix[nr]
          if (fnRow && matRow) {
            fnRow[nc] = true
            if (r >= 0 && r <= 6 && c >= 0 && c <= 6) {
              matRow[nc] =
                r === 0 || r === 6 || c === 0 || c === 6 || (r >= 2 && r <= 4 && c >= 2 && c <= 4)
            } else {
              matRow[nc] = false
            }
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
    const r6 = matrix[6]
    const fn6 = isFunction[6]
    const ri = matrix[i]
    const fni = isFunction[i]
    if (r6 && fn6) {
      r6[i] = i % 2 === 0
      fn6[i] = true
    }
    if (ri && fni) {
      ri[6] = i % 2 === 0
      fni[6] = true
    }
  }

  // Alignment patterns
  if (vInfo.align.length >= 2) {
    const coords = vInfo.align
    for (const r of coords) {
      for (const c of coords) {
        const checkFn = isFunction[r]?.[c]
        if (checkFn) continue
        for (let dr = -2; dr <= 2; dr++) {
          for (let dc = -2; dc <= 2; dc++) {
            const nr = r + dr
            const nc = c + dc
            const fnRow = isFunction[nr]
            const matRow = matrix[nr]
            if (fnRow && matRow) {
              fnRow[nc] = true
              matRow[nc] =
                Math.max(Math.abs(dr), Math.abs(dc)) === 2 || (dr === 0 && dc === 0)
            }
          }
        }
      }
    }
  }

  // Dark module
  const darkRow = matrix[size - 8]
  const darkFn = isFunction[size - 8]
  if (darkRow && darkFn) {
    darkRow[8] = true
    darkFn[8] = true
  }

  // Format info area reservation
  for (let i = 0; i < 9; i++) {
    const fn8 = isFunction[8]
    const fni = isFunction[i]
    const fnEnd = isFunction[size - 1 - i]
    if (fn8) {
      fn8[i] = true
      fn8[size - 1 - i] = true
    }
    if (fni) {
      fni[8] = true
    }
    if (fnEnd) {
      fnEnd[8] = true
    }
  }

  // Place data bits
  let bitIdx = 0
  const allBits: number[] = []
  for (const b of fullPayload) {
    for (let i = 7; i >= 0; i--) {
      allBits.push((b >> i) & 1)
    }
  }

  let upward = true
  for (let right = size - 1; right > 0; right -= 2) {
    if (right === 6) right--
    const rows = upward
      ? Array.from({ length: size }, (_, i) => size - 1 - i)
      : Array.from({ length: size }, (_, i) => i)

    for (const r of rows) {
      for (const c of [right, right - 1]) {
        const fnRow = isFunction[r]
        const matRow = matrix[r]
        if (fnRow && matRow && !fnRow[c]) {
          const bit = bitIdx < allBits.length ? (allBits[bitIdx++] ?? 0) : 0
          const mask = (r + c) % 2 === 0
          matRow[c] = (bit === 1) !== mask
        }
      }
    }
    upward = !upward
  }

  // Format Info for Mask 0, Level M
  const formatInfo = 0x5412
  for (let i = 0; i < 15; i++) {
    const bit = ((formatInfo >> i) & 1) === 1
    const r8 = matrix[8]
    if (r8) {
      if (i < 6) r8[i] = bit
      else if (i < 8) r8[i + 1] = bit
      else {
        const ri = matrix[14 - i]
        if (ri) ri[8] = bit
      }
    }

    if (i < 8) {
      const rEnd = matrix[size - 1 - i]
      if (rEnd) rEnd[8] = bit
    } else {
      const r8End = matrix[8]
      if (r8End) r8End[size - 15 + i] = bit
    }
  }

  // Build SVG path rects
  let rects = ''
  for (let r = 0; r < size; r++) {
    const row = matrix[r]
    if (!row) continue
    for (let c = 0; c < size; c++) {
      if (row[c]) {
        rects += `<rect x="${c}" y="${r}" width="1" height="1" fill="#0f172a"/>`
      }
    }
  }

  return `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 ${size} ${size}" width="${sizePx}" height="${sizePx}" fill="none">${rects}</svg>`
}
