const pad = (n) => String(n).padStart(2, '0');

export function money(n) {
  if (n === null || n === undefined || n === '') return '';
  return `${Number(n).toLocaleString('vi-VN')} đ`;
}

// Số tiền rút gọn cho trục biểu đồ
export function moneyShort(n) {
  const v = Number(n) || 0;
  if (v >= 1e9) return `${+(v / 1e9).toFixed(1)} tỷ`;
  if (v >= 1e6) return `${+(v / 1e6).toFixed(1)} tr`;
  if (v >= 1e3) return `${+(v / 1e3).toFixed(0)}k`;
  return String(v);
}

export function fmtDate(s) {
  if (!s) return '';
  const [y, m, d] = String(s).slice(0, 10).split('-');
  return `${d}/${m}/${y}`;
}

export function fmtDateTime(s) {
  return s ? `${fmtDate(s)} ${String(s).slice(11, 19)}` : '';
}

export function fmtMonth(s) {
  if (!s) return '';
  const [y, m] = s.split('-');
  return `${m}/${y}`;
}

export function localDate(d = new Date()) {
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
}

export function localMonth(d = new Date()) {
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}`;
}

export function localDateTimeInput(d = new Date()) {
  return `${localDate(d)}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

export function withSeconds(v) {
  if (!v) return null;
  return v.length === 16 ? `${v}:00` : v;
}

export function sum(list, pick) {
  return list.reduce((total, x) => total + (Number(pick(x)) || 0), 0);
}

export function firstDayOfMonth(d = new Date()) {
  return localDate(new Date(d.getFullYear(), d.getMonth(), 1));
}

// Trạng thái đợt ở theo [ngày bắt đầu, ngày kết thúc).
export function stayStatus(row, today = localDate()) {
  if (row.ngayBatDau > today) return { label: 'Chưa bắt đầu', tone: 'amber' };
  if (row.ngayKetThuc && row.ngayKetThuc <= today) return { label: 'Đã kết thúc', tone: 'gray' };
  return { label: 'Đang ở', tone: 'green' };
}
