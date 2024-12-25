package com.stellantis.team.utility.utils;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.table.JTableHeader;

import com.stellantis.team.utility.model.Status;
import com.stellantis.team.utility.view.Notification;

public class CommonUtils {
	public static void setUIFont(javax.swing.plaf.FontUIResource f) {
		@SuppressWarnings("rawtypes")
		Enumeration keys = UIManager.getDefaults().keys();
		while (keys.hasMoreElements()) {
			Object key = keys.nextElement();
			Object value = UIManager.get(key);
			if (value instanceof javax.swing.plaf.FontUIResource) {
				UIManager.put(key, f);
			}
		}
	}
	
	public static void customizeTableHeaders(JTableHeader header){
        header.setBackground(Color.LIGHT_GRAY);
	}
	
	public static void customizeTableRowHeight(JTable table){
		table.setRowHeight(30);
	}
	
	public static void setButtonFontToBold(JButton button){
		Font boldFont = new Font(button.getFont().getName(), Font.BOLD, button.getFont().getSize());
        button.setFont(boldFont);
	}
	
	public static void setTableHeaderFontToBold(JTableHeader header){
		Font boldFont = new Font(header.getFont().getName(), Font.BOLD, header.getFont().getSize());
		header.setFont(boldFont);
	}
	
	public static void setTextFieldHeight(JTextField textField){
		Dimension preferredSize = new Dimension(textField.getPreferredSize().width, 35); // Increase the height (40 in this example)
        textField.setPreferredSize(preferredSize);
	}
	
	/**
	 * Sort the value of map in Alphabetic order for String
	 * @param map
	 * @return
	 */
	public static Map<String, String> sortValueSetOfMap(Map<String, String> map) {
		List<Map.Entry<String, String>> entryList = new ArrayList<>(map.entrySet());

		Collections.sort(entryList, new Comparator<Map.Entry<String, String>>() {
			@Override
			public int compare(Map.Entry<String, String> entry1, Map.Entry<String, String> entry2) {
				return entry1.getValue().compareTo(entry2.getValue());
			}
		});

		Map<String, String> sortedMap = new LinkedHashMap<>();
		for (Map.Entry<String, String> entry : entryList) {
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	}
	
	public static Map<Object, String> sortValueSetOfMapOfObject(Map<Object, String> map) {
		List<Map.Entry<Object, String>> entryList = new ArrayList<>(map.entrySet());

		Collections.sort(entryList, new Comparator<Map.Entry<Object, String>>() {
			@Override
			public int compare(Map.Entry<Object, String> entry1, Map.Entry<Object, String> entry2) {
				return entry1.getValue().compareTo(entry2.getValue());
			}
		});

		Map<Object, String> sortedMap = new LinkedHashMap<>();
		for (Map.Entry<Object, String> entry : entryList) {
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	}
	
	public static void openNotepadFile(String filePath) {
		try {
			File file = new File(filePath);
			if (file.exists()) {
				Desktop desktop = Desktop.getDesktop();
				desktop.open(file);
			} else {
				Notification.addMessage(Status.INFO.toString(), "There is no Log file.");
			}
		} catch (IOException ex) {
			CustomLogger.logException(ex);
		}
	}
}
