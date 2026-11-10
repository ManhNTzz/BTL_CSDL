import Icon from './Icon.jsx';

export default function DataTable({ columns, rows, rowKey, loading, empty = 'Không có dữ liệu', selection, foot }) {
  const all = rows.length > 0 && selection && rows.every((r) => selection.selected.has(rowKey(r)));

  function toggleAll() {
    selection.onChange(all ? new Set() : new Set(rows.map(rowKey)));
  }

  function toggleOne(key) {
    const next = new Set(selection.selected);
    if (next.has(key)) next.delete(key);
    else next.add(key);
    selection.onChange(next);
  }

  return (
    <div className="table-wrap">
      <table className="table">
        <thead>
          <tr>
            {selection && (
              <th className="col-check">
                <input type="checkbox" checked={all} onChange={toggleAll} aria-label="Chọn tất cả" />
              </th>
            )}
            {columns.map((c) => (
              <th key={c.key} className={`${c.align ? `ta-${c.align}` : ''} ${c.className || ''}`}>
                {c.title}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {rows.map((row) => {
            const key = rowKey(row);
            return (
              <tr key={key} className={selection?.selected.has(key) ? 'is-selected' : ''}>
                {selection && (
                  <td className="col-check">
                    <input
                      type="checkbox"
                      checked={selection.selected.has(key)}
                      onChange={() => toggleOne(key)}
                      aria-label="Chọn dòng"
                    />
                  </td>
                )}
                {columns.map((c) => (
                  <td key={c.key} className={`${c.align ? `ta-${c.align}` : ''} ${c.className || ''}`}>
                    {c.render ? c.render(row) : row[c.key]}
                  </td>
                ))}
              </tr>
            );
          })}
        </tbody>
        {foot && <tfoot>{foot}</tfoot>}
      </table>
      {loading && rows.length === 0 && <div className="table-state">Đang tải dữ liệu...</div>}
      {!loading && rows.length === 0 && <div className="table-state">{empty}</div>}
    </div>
  );
}

export function RowActions({ children }) {
  return <div className="row-actions">{children}</div>;
}

export function IconButton({ icon, title, onClick, tone }) {
  return (
    <button type="button" className={`icon-btn${tone ? ` icon-btn-${tone}` : ''}`} title={title} aria-label={title} onClick={onClick}>
      <Icon name={icon} size={16} />
    </button>
  );
}
