import QRCode from 'qrcode'

/**
 * Sinh QR Code dạng SVG string từ thư viện `qrcode` chuẩn ISO/IEC 18004.
 * @param text   Nội dung cần encode (URL, mã lô…)
 * @param sizePx Kích thước SVG xuất ra (px), mặc định 160px
 */
export async function generateQrSvg(text: string, sizePx = 160): Promise<string> {
  return QRCode.toString(text, {
    type: 'svg',
    width: sizePx,
    margin: 2,
    errorCorrectionLevel: 'M',
    color: {
      dark: '#0f172a',
      light: '#ffffff',
    },
  })
}

/**
 * Sinh QR Code dạng Data URL (PNG) để nhúng vào <img src=...>
 */
export async function generateQrDataUrl(text: string, sizePx = 160): Promise<string> {
  return QRCode.toDataURL(text, {
    width: sizePx,
    margin: 2,
    errorCorrectionLevel: 'M',
    color: {
      dark: '#0f172a',
      light: '#ffffff',
    },
  })
}
