import { useMemo } from 'react';
import { api } from '../api/endpoints.js';
import { useAsync } from './useAsync.js';

// Danh sách lựa chọn cho các ô <select> (sinh viên, phòng, dịch vụ, xe).
export function useSinhVienOptions() {
  const res = useAsync(() => api.sinhVien.list(''), []);
  const options = useMemo(
    () => (res.data || []).map((s) => ({ value: s.maSV, label: `${s.maSV} - ${s.hoTen}` })),
    [res.data],
  );
  return { ...res, options };
}

export function usePhongOptions() {
  const res = useAsync(() => api.phong.list({}), []);
  const options = useMemo(
    () =>
      (res.data || []).map((p) => ({
        value: p.soPhong,
        label: `${p.soPhong} - ${p.loaiPhong} (còn ${p.conCho}/${p.soNguoiToiDa} chỗ)`,
      })),
    [res.data],
  );
  return { ...res, options };
}

export function useDichVuOptions() {
  const res = useAsync(() => api.dichVu.list(), []);
  const options = useMemo(
    () => (res.data || []).map((d) => ({ value: d.maDV, label: `${d.tenDV} (${d.maDV})` })),
    [res.data],
  );
  return { ...res, options };
}

export function useXeOptions() {
  const res = useAsync(() => api.xe.list(''), []);
  const options = useMemo(
    () => (res.data || []).map((x) => ({ value: x.bienSo, label: `${x.bienSo} - ${x.hoTenChuXe}` })),
    [res.data],
  );
  return { ...res, options };
}
