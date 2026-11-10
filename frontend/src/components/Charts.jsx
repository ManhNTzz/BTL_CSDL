import { money, moneyShort } from '../utils/format.js';

function niceMax(v) {
  if (v <= 0) return 1;
  const pow = 10 ** Math.floor(Math.log10(v));
  const n = v / pow;
  const step = n <= 1 ? 1 : n <= 2 ? 2 : n <= 5 ? 5 : 10;
  return step * pow;
}
// Biểu đồ cột chồng.
export function StackedBarChart({ data, series, height = 260 }) {
  const width = 640;
  const pad = { top: 16, right: 12, bottom: 34, left: 52 };
  const innerW = width - pad.left - pad.right;
  const innerH = height - pad.top - pad.bottom;
  const totals = data.map((d) => series.reduce((s, x) => s + (Number(d.values[x.key]) || 0), 0));
  const max = niceMax(Math.max(0, ...totals));
  const ticks = [0, 0.25, 0.5, 0.75, 1].map((f) => max * f);
  const slot = data.length ? innerW / data.length : innerW;
  const barW = Math.min(56, slot * 0.6);

  return (
    <div className="chart">
      <svg viewBox={`0 0 ${width} ${height}`} role="img" aria-label="Biểu đồ cột">
        {ticks.map((t) => {
          const y = pad.top + innerH - (t / max) * innerH;
          return (
            <g key={t}>
              <line x1={pad.left} x2={width - pad.right} y1={y} y2={y} className="chart-grid" />
              <text x={pad.left - 8} y={y + 4} textAnchor="end" className="chart-axis">
                {moneyShort(t)}
              </text>
            </g>
          );
        })}
        {data.map((d, i) => {
          const x = pad.left + slot * i + (slot - barW) / 2;
          let yCursor = pad.top + innerH;
          return (
            <g key={d.label}>
              {series.map((s) => {
                const v = Number(d.values[s.key]) || 0;
                const h = (v / max) * innerH;
                yCursor -= h;
                return h > 0 ? (
                  <rect key={s.key} x={x} y={yCursor} width={barW} height={h} fill={s.color} rx="2">
                    <title>{`${d.label} - ${s.label}: ${money(v)}`}</title>
                  </rect>
                ) : null;
              })}
              <text x={x + barW / 2} y={height - 12} textAnchor="middle" className="chart-axis">
                {d.label}
              </text>
            </g>
          );
        })}
      </svg>
      <Legend items={series} />
    </div>
  );
}

function Legend({ items }) {
  return (
    <ul className="legend">
      {items.map((s) => (
        <li key={s.key}>
          <i style={{ background: s.color }} /> {s.label}
        </li>
      ))}
    </ul>
  );
}

// Biểu đồ tròn rỗng giữa. items: [{key, label, value, color}]
export function Donut({ items, centerLabel }) {
  const total = items.reduce((s, x) => s + (Number(x.value) || 0), 0);
  const r = 54;
  const c = 2 * Math.PI * r;
  let offset = 0;
  return (
    <div className="donut">
      <svg viewBox="0 0 140 140" width="160" height="160" role="img" aria-label="Biểu đồ tròn">
        <circle cx="70" cy="70" r={r} fill="none" stroke="#e8edf3" strokeWidth="18" />
        {total > 0 &&
          items.map((it) => {
            const len = ((Number(it.value) || 0) / total) * c;
            const el = (
              <circle
                key={it.key}
                cx="70"
                cy="70"
                r={r}
                fill="none"
                stroke={it.color}
                strokeWidth="18"
                strokeDasharray={`${len} ${c - len}`}
                strokeDashoffset={-offset}
                transform="rotate(-90 70 70)"
              >
                <title>{`${it.label}: ${money(it.value)}`}</title>
              </circle>
            );
            offset += len;
            return el;
          })}
        <text x="70" y="66" textAnchor="middle" className="donut-total">
          {moneyShort(total)}
        </text>
        <text x="70" y="84" textAnchor="middle" className="donut-caption">
          {centerLabel}
        </text>
      </svg>
      <ul className="legend legend-col">
        {items.map((it) => (
          <li key={it.key}>
            <i style={{ background: it.color }} /> {it.label}
            <b>{total > 0 ? `${Math.round(((Number(it.value) || 0) / total) * 100)}%` : '0%'}</b>
          </li>
        ))}
      </ul>
    </div>
  );
}

// Biểu đồ cột ngang cho một dãy giá trị. items: [{key, label, value}]
export function HBarChart({ items, color = '#1769e0' }) {
  const max = Math.max(1, ...items.map((i) => Number(i.value) || 0));
  return (
    <ul className="hbars">
      {items.map((it) => (
        <li key={it.key}>
          <span className="hbar-label">{it.label}</span>
          <span className="hbar-track">
            <span className="hbar-fill" style={{ width: `${((Number(it.value) || 0) / max) * 100}%`, background: color }} />
          </span>
          <b className="hbar-value">{money(it.value)}</b>
        </li>
      ))}
    </ul>
  );
}
