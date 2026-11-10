// Gọi REST API của backend
import * as XLSX from 'xlsx';

const BASE = (import.meta.env && import.meta.env.VITE_API_URL) || '';

export class ApiError extends Error {
  constructor(message, status, details) {
    super(message);
    this.status = status;
    this.details = details || [];
  }
}

function query(params) {
  const sp = new URLSearchParams();
  Object.entries(params || {}).forEach(([k, v]) => {
    if (v !== undefined && v !== null && v !== '') sp.append(k, v);
  });
  const s = sp.toString();
  return s ? `?${s}` : '';
}

async function toError(res) {
  let body = null;
  try {
    body = await res.json();
  } catch {
    // phản hồi không có nội dung JSON
  }
  const fallback = res.status === 404 ? 'Không tìm thấy dữ liệu' : `Lỗi máy chủ (${res.status})`;
  return new ApiError(body?.message || fallback, res.status, body?.details);
}

export async function request(method, path, { params, body } = {}) {
  let res;
  try {
    res = await fetch(`${BASE}/api${path}${query(params)}`, {
      method,
      headers: body !== undefined ? { 'Content-Type': 'application/json' } : undefined,
      body: body !== undefined ? JSON.stringify(body) : undefined,
    });
  } catch {
    throw new ApiError('Mất kết nối với ứng dụng. Hãy kiểm tra backend và cấu hình kết nối.', 0);
  }
  if (!res.ok) throw await toError(res);
  if (res.status === 204) return null;
  return res.json();
}

// Tải file từ backend và xuất thành Excel (.xlsx
export async function download(path, params, fileName) {
  let res;
  try {
    res = await fetch(`${BASE}/api${path}${query(params)}`);
  } catch {
    throw new ApiError('Không kết nối được máy chủ.', 0);
  }
  if (!res.ok) throw await toError(res);

  const buffer = await res.arrayBuffer();
  const decoder = new TextDecoder('utf-8');
  let text = decoder.decode(buffer);

  text = text.replace(/^\uFEFF/, '').replace(/^sep=[^\r\n]*\r?\n/i, '');

  let blob;
  let finalFileName = fileName;

  const isXlsx = fileName.endsWith('.xlsx') || !fileName.endsWith('.csv');
  if (isXlsx) {
    if (!finalFileName.endsWith('.xlsx')) {
      finalFileName = finalFileName.replace(/\.csv$/i, '') + '.xlsx';
    }

    const wb = XLSX.read(text, { type: 'string', raw: true });
    const sheetName = wb.SheetNames[0] || 'BaoCao';
    const ws = wb.Sheets[sheetName];

    const range = XLSX.utils.decode_range(ws['!ref'] || 'A1');
    const widths = [];
    for (let c = range.s.c; c <= range.e.c; c++) {
      const heading = String(ws[XLSX.utils.encode_cell({r: 0, c})]?.v || '');
      const numeric = /tiền|doanh thu|đơn giá|số lượng|số lần|tổng giá/i.test(heading);
      let width = Math.max(12, heading.length);
      for (let r = 1; r <= range.e.r; r++) {
        const cell = ws[XLSX.utils.encode_cell({r,c})]; if (!cell) continue;
        width = Math.max(width, String(cell.v ?? '').length);
        if (numeric && /^-?\d+(\.\d+)?$/.test(String(cell.v))) { cell.v=Number(cell.v); cell.t='n'; cell.z='#,##0'; }
        else { cell.v=String(cell.v ?? ''); cell.t='s'; delete cell.f; }
      }
      widths[c] = { wch: Math.min(48,width+3) };
    }
    ws['!cols'] = widths;

    const xlsxBuffer = XLSX.write(wb, { bookType: 'xlsx', type: 'array' });
    blob = new Blob([xlsxBuffer], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    });
  } else {

    const BOM = new Uint8Array([0xEF, 0xBB, 0xBF]);
    blob = new Blob([BOM, text], { type: 'text/csv;charset=utf-8;' });
  }

  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = finalFileName;
  document.body.appendChild(a);
  a.click();
  a.remove();
  setTimeout(() => URL.revokeObjectURL(url), 1000);
}