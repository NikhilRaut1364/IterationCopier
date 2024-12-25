package com.stellantis.team.utility.view;

import java.awt.Color;
import java.awt.Component;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import com.stellantis.team.utility.utils.CommonUtils;
import com.stellantis.team.utility.utils.CustomLogger;

@SuppressWarnings("serial")
public class Notification extends JTable {
	private static DefaultTableModel model = new DefaultTableModel();

	@Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }
	
	public Notification() {
		CommonUtils.customizeTableRowHeight(Notification.this);
		CommonUtils.customizeTableHeaders(getTableHeader());
		CommonUtils.setTableHeaderFontToBold(getTableHeader());
		model.addColumn("Date");
		model.addColumn("Status");
		model.addColumn("Message");

		setModel(model);

		TableColumnModel columnModel = getColumnModel();
		columnModel.getColumn(0).setPreferredWidth(10); // Date column width
		columnModel.getColumn(1).setPreferredWidth(10);
		columnModel.getColumn(2).setPreferredWidth(900);

		setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		
		// Set custom renderer for row coloring
        setDefaultRenderer(Object.class, new StatusColorRenderer());
	}

	public static void addMessage(String status, String message) {
		CustomLogger.logMessage(message);
		LocalDateTime currentDateTime = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		String formattedDateTime = currentDateTime.format(formatter);
		DefaultTableModel model1 = (DefaultTableModel) model;
		model1.insertRow(0, new Object[] { formattedDateTime, status, message });
		model1.fireTableDataChanged();
	}
	
	class StatusColorRenderer implements TableCellRenderer {
        private TableCellRenderer defaultRenderer;

        public StatusColorRenderer() {
            this.defaultRenderer = getDefaultRenderer(Object.class);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = defaultRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String status = (String) table.getValueAt(row, 1); // Get the status from the "Status" column

            if (status != null) {
                switch (status) {
                    case "SUCCESSFUL":
                        c.setBackground(new Color(144, 238, 144));
                        break;
                    case "INFO":
                        c.setBackground(new Color(173, 216, 230));
                        break;
                    case "ERROR":
                        c.setBackground(new Color(255, 182, 193));
                        break;
                    case "WARNING":
                        c.setBackground(new Color(255, 223, 186));
                        break;
                    default:
                        c.setBackground(table.getBackground());
                        break;
                }
            } else {
                c.setBackground(table.getBackground());
            }

            return c;
        }
    }
}
