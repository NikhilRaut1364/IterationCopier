package com.stellantis.team.utility.service;

import java.util.ArrayList;
import java.util.List;

import com.ibm.team.process.common.IProjectArea;
import com.stellantis.team.utility.controller.ProcessAreaController;
import com.stellantis.team.utility.model.Node;
import com.stellantis.team.utility.model.Status;
import com.stellantis.team.utility.utils.CustomLogger;

public class IterationHierarchyWorker extends UtilitySwingWorker{
	private IProjectArea sourceProjectArea;
	private List<Node> hierarchy;

	public List<Node> getHierarchy() {
		if(hierarchy == null)
			hierarchy = new ArrayList<>();
		return hierarchy;
	}

	public void setHierarchy(List<Node> hierarchy) {
		this.hierarchy = hierarchy;
	}

	public IterationHierarchyWorker(IProjectArea sourceProjectArea) {
		this.sourceProjectArea = sourceProjectArea;
	}
	
	@Override
	protected Boolean doInBackground() throws Exception {
		try {
			publish(Status.INFO.toString() + "@" + "Please wait while fetching the Iterations for " + sourceProjectArea.getName());
			ProcessAreaController processAreaController = new ProcessAreaController();
			List<Node> iterationHierarchy = processAreaController.getIterationHierarchyFromSource(sourceProjectArea);
			publish(Status.SUCCESSFUL.toString() + "@" + "Iteration hierarchy for Project Area [" + sourceProjectArea.getName() + "] has been fetched.");
			
			setHierarchy(iterationHierarchy);
			if(iterationHierarchy.size() > 0)
				return true;
			return false;
		} catch (Exception e) {
			CustomLogger.logException(e);
		}
		return false;
	}
}
