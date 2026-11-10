package com.baitapnhom.quanlyktx.util;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class CsvUtil {

    private CsvUtil() {
    }

    public static byte[] build(List<String> tieuDe, List<List<Object>> cacDong) {
        StringBuilder sb = new StringBuilder("\uFEFF");
        ghiDong(sb, new ArrayList<Object>(tieuDe));
        for (List<Object> dong : cacDong) {
            ghiDong(sb, dong);
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static void ghiDong(StringBuilder sb, List<Object> cacO) {
        for (int i = 0; i < cacO.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(escape(cacO.get(i)));
        }
        sb.append("\r\n");
    }

    private static String escape(Object giaTri) {
        if (giaTri == null) {
            return "";
        }
        String s = giaTri instanceof BigDecimal b ? b.toPlainString() : giaTri.toString();
        if (s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }
}
