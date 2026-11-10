import { api } from '../api/endpoints.js';
import { useAsync } from '../hooks/useAsync.js';
import { fmtDate, fmtDateTime, fmtMonth, money, stayStatus } from '../utils/format.js';
import { Badge, ErrorBox } from './Ui.jsx';
import DataTable from './DataTable.jsx';
import Modal from './Modal.jsx';

// Hồ sơ đầy đủ của một sinh viên: thông tin, phòng, xe, dịch vụ, khách đến thăm, hóa đơn 
export default function HoSoModal({ maSV, onClose }) {
  const res = useAsync(() => api.sinhVien.hoSo(maSV), [maSV]);
  const h = res.data;

  return (
    <Modal title={`Hồ sơ sinh viên ${maSV}`} onClose={onClose} size="xl">
      <ErrorBox error={res.error} onRetry={res.reload} />
      {res.loading && !h && <div className="table-state">Đang tải hồ sơ...</div>}
      {h && (
        <div className="profile">
          <dl className="kv">
            <div><dt>Họ và tên</dt><dd>{h.thongTin.hoTen}</dd></div>
            <div><dt>Mã SV</dt><dd>{h.thongTin.maSV}</dd></div>
            <div><dt>Số CCCD</dt><dd>{h.thongTin.cccd}</dd></div>
            <div><dt>Ngày sinh</dt><dd>{fmtDate(h.thongTin.ngaySinh)}</dd></div>
            <div><dt>Lớp</dt><dd>{h.thongTin.lop}</dd></div>
            <div><dt>Quê quán</dt><dd>{h.thongTin.queQuan}</dd></div>
            <div>
              <dt>Phòng hiện tại</dt>
              <dd>
                {h.phongHienTai ? (
                  <>
                    {h.phongHienTai.soPhong} <span className="muted">({h.phongHienTai.loaiPhong})</span>
                  </>
                ) : (
                  <Badge tone="amber">Chưa ở phòng nào</Badge>
                )}
              </dd>
            </div>
            <div>
              <dt>Xe gửi vé tháng</dt>
              <dd>{h.xe.length ? h.xe.map((x) => `${x.bienSo} (${x.loaiXe})`).join(', ') : 'Không có'}</dd>
            </div>
          </dl>

          <h4>Lịch sử ở phòng</h4>
          <DataTable
            rows={h.lichSuPhong}
            rowKey={(r) => `${r.soPhong}|${r.ngayBatDau}`}
            empty="Chưa có lịch sử ở phòng"
            columns={[
              { key: 'soPhong', title: 'Phòng' },
              { key: 'loaiPhong', title: 'Loại phòng' },
              { key: 'donGia', title: 'Đơn giá/tháng', align: 'right', render: (r) => money(r.donGia) },
              { key: 'ngayBatDau', title: 'Từ ngày', render: (r) => fmtDate(r.ngayBatDau) },
              {
                key: 'ngayKetThuc',
                title: 'Đến ngày',
                render: (r) => (r.ngayKetThuc ? fmtDate(r.ngayKetThuc) : <span className="muted">Chưa xác định</span>),
              },
    { key: 'trangThai', title: 'Trạng thái', render: (r) => { const t = stayStatus(r); return <Badge tone={t.tone}>{t.label}</Badge>; } },
            ]}
          />

          <h4>Hóa đơn các tháng</h4>
          <DataTable
            rows={h.hoaDon}
            rowKey={(r) => r.thang}
            empty="Chưa có hóa đơn"
            columns={[
              { key: 'thang', title: 'Tháng', render: (r) => fmtMonth(r.thang) },
              { key: 'phongTinhTien', title: 'Phòng tính tiền' },
              { key: 'tienPhong', title: 'Tiền phòng', align: 'right', render: (r) => money(r.tienPhong) },
              { key: 'tienDichVu', title: 'Dịch vụ', align: 'right', render: (r) => money(r.tienDichVu) },
              {
                key: 'xe',
                title: 'Gửi xe',
                align: 'right',
                render: (r) => money(Number(r.tienVeThang) + Number(r.tienPhatSinhXe)),
              },
              { key: 'tongTien', title: 'Tổng tiền', align: 'right', render: (r) => <b>{money(r.tongTien)}</b> },
            ]}
          />

          <h4>Dịch vụ đã sử dụng</h4>
          <DataTable
            rows={h.dichVuDaDung}
            rowKey={(r) => `${r.maDV}|${r.thoiGianSD}`}
            empty="Chưa sử dụng dịch vụ nào"
            columns={[
              { key: 'thoiGianSD', title: 'Thời gian', render: (r) => fmtDateTime(r.thoiGianSD) },
              { key: 'tenDV', title: 'Dịch vụ' },
              { key: 'soLuong', title: 'Số lượng', align: 'right' },
              { key: 'thanhTien', title: 'Thành tiền', align: 'right', render: (r) => money(r.thanhTien) },
            ]}
          />

          <h4>Vé tháng gửi xe</h4>
          <DataTable
            rows={h.veThang}
            rowKey={(r) => `${r.bienSo}|${r.thang}`}
            empty="Chưa đăng ký vé tháng"
            columns={[
              { key: 'bienSo', title: 'Biển số' },
              { key: 'thang', title: 'Tháng', render: (r) => fmtMonth(r.thang) },
              { key: 'donGiaVe', title: 'Đơn giá', align: 'right', render: (r) => money(r.donGiaVe) },
            ]}
          />

          <h4>Khách đến thăm</h4>
          <DataTable
            rows={h.khachDenTham}
            rowKey={(r) => `${r.cccdKhach}|${r.ngayDenChoi}`}
            empty="Chưa có khách đến thăm"
            columns={[
              { key: 'ngayDenChoi', title: 'Thời gian', render: (r) => fmtDateTime(r.ngayDenChoi) },
              { key: 'hoTenKhach', title: 'Khách' },
              { key: 'cccdKhach', title: 'CCCD khách' },
            ]}
          />
        </div>
      )}
    </Modal>
  );
}
