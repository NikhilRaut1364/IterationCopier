package com.stellantis.team.utility.service;

import java.util.List;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

import com.ibm.team.process.common.IProjectArea;
import com.stellantis.team.utility.controller.ProcessAreaController;
import com.stellantis.team.utility.utils.CustomLogger;

public class CopyWorker extends UtilitySwingWorker{

	private IProjectArea sourceProjectArea;
	private IProjectArea targetProjectArea;
	@SuppressWarnings("unused")
	private List<IProjectArea> lstTargetProjectArea;
	private DefaultMutableTreeNode sourceSelectedNode;
	private DefaultMutableTreeNode targetSelectedNode;
	private JTree sourceIterationTree;

	public CopyWorker(IProjectArea sourceProjectArea, IProjectArea targetProjectArea,
			List<IProjectArea> lstTargetProjectArea, DefaultMutableTreeNode sourceSelectedNode,
			DefaultMutableTreeNode targetSelectedNode, JTree sourceIterationTree) {
		this.sourceProjectArea = sourceProjectArea;
		this.targetProjectArea = targetProjectArea;
		this.lstTargetProjectArea = lstTargetProjectArea;
		this.sourceSelectedNode = sourceSelectedNode;
		this.targetSelectedNode = targetSelectedNode;
		this.sourceIterationTree = sourceIterationTree;
	}
	
	@Override
	protected Boolean doInBackground() throws Exception {
		try {
			ProcessAreaController processAreaController = new ProcessAreaController();
			
			/* BELOW FUNCTION IS TO COPY ENTIRE ITERATION FROM SOURCE TO TARGET AREA */
//			boolean copyIterationHierarchy = processAreaController.copyIterationHierarchy(sourceProjectArea, lstTargetProjectArea);
			
			/* BELOW FUNCTION IS TO COPY SELECTED ITERATION FROM SOURCE TO TARGET AREA */
//			boolean copyIterationHierarchy = processAreaController.copySpecificIterationFromSource(sourceSelectedNode, sourceProjectArea, lstTargetProjectArea);
			
			/* BELOW FUNCTION IS TO COPY SELECTED ITERATION FROM SOURCE TO SELECTED TARGET AREA */
			boolean copyIterationHierarchy = processAreaController.processCopyIterationToTarget(sourceSelectedNode, targetSelectedNode, sourceProjectArea, targetProjectArea, sourceIterationTree);
			return copyIterationHierarchy;
		} catch (Exception e) {
			CustomLogger.logException(e);
		}
		return false;
	}
}
