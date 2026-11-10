// Mỗi hàm ứng với một API của backend .
import { download, request } from './client.js';

const get = (path, params) => request('GET', path, { params });
const post = (path, body, params) => request('POST', path, { body, params });
const put = (path, body) => request('PUT', path, { body });
const del = (path, params) => request('DELETE', path, { params });
const e = encodeURIComponent;

export const api = {
  tongQuan: () => get('/thong-ke/tong-quan'),
  doanhThuTheoThang: () => get('/thong-ke/doanh-thu-theo-thang'),
  canhBao: () => get('/canh-bao'),

  sinhVien: {
    list: (tuKhoa) => get('/sinh-vien', { tuKhoa }),
    hoSo: (maSV) => get(`/sinh-vien/${e(maSV)}/ho-so`),
    create: (body) => post('/sinh-vien', body),
    update: (maSV, body) => put(`/sinh-vien/${e(maSV)}`, body),
    remove: (maSV) => del(`/sinh-vien/${e(maSV)}`),
  },
  phong: {
    list: (params) => get('/phong', params),
    sinhVien: (soPhong, ngay) => get(`/phong/${e(soPhong)}/sinh-vien`, { ngay }),
    create: (body) => post('/phong', body),
    update: (soPhong, body) => put(`/phong/${e(soPhong)}`, body),
    remove: (soPhong) => del(`/phong/${e(soPhong)}`),
  },
  oPhong: {
    lichSu: (maSV) => get('/o-phong', { maSV }),
    dangKy: (body) => post('/o-phong', body),
    ketThuc: (body) => put('/o-phong/ket-thuc', body),
    chuyen: (body) => post('/o-phong/chuyen', body),
  },
  dichVu: {
    list: () => get('/dich-vu'),
    create: (body) => post('/dich-vu', body),
    update: (maDV, body) => put(`/dich-vu/${e(maDV)}`, body),
    remove: (maDV) => del(`/dich-vu/${e(maDV)}`),
  },
  suDung: {
    list: (params) => get('/su-dung-dich-vu', params),
    create: (body) => post('/su-dung-dich-vu', body),
    remove: (params) => del('/su-dung-dich-vu', params),
  },
  khach: {
    list: () => get('/khach'),
    luotTham: (params) => get('/luot-tham', params),
    ghiNhan: (body) => post('/luot-tham', body),
    huyLuot: (params) => del('/luot-tham', params),
  },
  xe: {
    list: (maSV) => get('/xe', { maSV }),
    create: (body) => post('/xe', body),
    remove: (bienSo) => del(`/xe/${e(bienSo)}`),
  },
  veThang: {
    list: (bienSo) => get('/ve-thang', { bienSo }),
    create: (body) => post('/ve-thang', body),
    giaHan: (thang) => post('/ve-thang/gia-han', undefined, { thang }),
  },
  luotXe: {
    list: (params) => get('/luot-gui-lay-xe', params),
    dangGui: () => get('/luot-gui-lay-xe/dang-gui'),
    gui: (body) => post('/luot-gui-lay-xe', body),
    lay: (body) => put('/luot-gui-lay-xe/lay', body),
  },
  baoCao: {
    hoaDon: (params) => get('/bao-cao/hoa-don', params),
    dichVuSinhVien: (params) => get('/bao-cao/dich-vu-sinh-vien', params),
    khachTham: (params) => get('/bao-cao/khach-tham', params),
    doanhThuDichVu: (params) => get('/bao-cao/doanh-thu-dich-vu', params),
    csv: (loai, params, fileName) => download(`/bao-cao/${loai}/csv`, params, fileName),
  },
};
