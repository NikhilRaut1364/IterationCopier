package com.stellantis.team.utility.service;

import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import com.stellantis.team.utility.view.Notification;

public class UtilitySwingWorker extends SwingWorker<Boolean, String>{

	@Override
	protected Boolean doInBackground() throws Exception {
		return null;
	}
	
	@Override
	protected void process(List<String> chunks) {
		String status = chunks.get(chunks.size() - 1);
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				String[] split = status.split("@");
				if(split.length == 2)
					Notification.addMessage(split[0], split[1]);
			}
		});
	}

}
