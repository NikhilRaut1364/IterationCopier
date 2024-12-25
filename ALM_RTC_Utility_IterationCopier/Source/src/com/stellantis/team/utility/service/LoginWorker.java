package com.stellantis.team.utility.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;

import com.ibm.team.process.common.IProjectArea;
import com.ibm.team.repository.client.TeamPlatform;
import com.stellantis.team.utility.controller.LoginController;
import com.stellantis.team.utility.controller.ProcessAreaController;
import com.stellantis.team.utility.model.KeyValuePair;
import com.stellantis.team.utility.model.ProjectAreaPair;
import com.stellantis.team.utility.model.Status;
import com.stellantis.team.utility.utils.CustomLogger;
import com.stellantis.team.utility.utils.SysoutProgressMonitor;

public class LoginWorker extends UtilitySwingWorker{
	private String username;
	private String password;
	private KeyValuePair serverComboBoxSelection;
	private IProgressMonitor monitor = new SysoutProgressMonitor();
	private List<ProjectAreaPair> fetchAllProjectAreas;
	
	public List<ProjectAreaPair> getFetchAllProjectAreas() {
		if(fetchAllProjectAreas == null)
			fetchAllProjectAreas = new ArrayList<>();
		return fetchAllProjectAreas;
	}

	public void setFetchAllProjectAreas(List<ProjectAreaPair> fetchAllProjectAreas) {
		this.fetchAllProjectAreas = fetchAllProjectAreas;
	}

	public LoginWorker(KeyValuePair serverComboBoxSelection, String username, String password) {
		this.serverComboBoxSelection = serverComboBoxSelection;
		this.username = username;
		this.password = password;
	}

	@Override
	protected Boolean doInBackground() throws Exception {
		boolean isValid = false;
		try {
			publish(Status.INFO.toString() + "@" + "Please wait as we establish a secure connection [" + serverComboBoxSelection.getValue() + "].");
			TeamPlatform.startup();
			boolean login = new LoginController().login(serverComboBoxSelection.getValue(), username, password, monitor);

			if (login) {
				publish(Status.SUCCESSFUL.toString() + "@" + "Login successful. Please wait as we retrieve your project area information.");
				ProcessAreaController processAreaController = new ProcessAreaController();
				Map<Object, String> getAllProjectArea = processAreaController.fetchAllProjectAreas();
				for (Map.Entry<Object, String> entry : getAllProjectArea.entrySet()){
					if(entry.getKey() instanceof IProjectArea){
						IProjectArea projectAreaObj = (IProjectArea) entry.getKey();
						getFetchAllProjectAreas().add(new ProjectAreaPair(entry.getValue(), projectAreaObj));
					}
				}
				isValid = true;
			}
			else
				publish(Status.ERROR.toString() + "@" + "Login Failed.");
			return isValid;
		} catch (Exception e) {
			CustomLogger.logException(e);
		}
		
		return isValid;
	}
	
}
