package com.stellantis.team.utility.controller;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import com.ibm.team.process.client.IProcessClientService;
import com.ibm.team.process.client.IProcessItemService;
import com.ibm.team.process.common.IDevelopmentLine;
import com.ibm.team.process.common.IDevelopmentLineHandle;
import com.ibm.team.process.common.IIteration;
import com.ibm.team.process.common.IIterationHandle;
import com.ibm.team.process.common.IProcessArea;
import com.ibm.team.process.common.IProcessItem;
import com.ibm.team.process.common.IProjectArea;
import com.ibm.team.process.internal.common.Iteration;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.stellantis.team.utility.model.IterationPair;
import com.stellantis.team.utility.model.Node;
import com.stellantis.team.utility.model.Status;
import com.stellantis.team.utility.model.TeamRepositoryInstance;
import com.stellantis.team.utility.utils.CommonUtils;
import com.stellantis.team.utility.utils.CustomLogger;
import com.stellantis.team.utility.view.Notification;

public class ProcessAreaController {
	private TeamRepositoryInstance teamRepositoryInstance;
	
	public ProcessAreaController() {
		this.teamRepositoryInstance = TeamRepositoryInstance.getInstance();
	}
	
	@SuppressWarnings("rawtypes")
	public Map<Object, String> fetchAllProjectAreas() {
		CustomLogger.logMessage("fetchAllProjectAreas");
		Map<Object, String> mapProcessArea = new HashMap<>();
		try {
			IProcessItemService itemService = (IProcessItemService) teamRepositoryInstance.getRepo().getClientLibrary(IProcessItemService.class);
			List findAllProjectAreas = itemService.findAllProjectAreas(null, null);
			for (Object object : findAllProjectAreas) {
				if (object instanceof IProjectArea) {
					IProjectArea proArea = (IProjectArea) object;
					if(!proArea.isArchived()){
						mapProcessArea.put(proArea, proArea.getName());
					}
				}
			}

			return CommonUtils.sortValueSetOfMapOfObject(mapProcessArea);
		} catch (Exception e) {
			CustomLogger.logException(e);
		}
		return mapProcessArea;
	}
	
	public IProcessArea getProjectArea(String projectAreaName){
		CustomLogger.logMessage("getProjectArea");
		try {
			IProcessClientService processClientService = (IProcessClientService) teamRepositoryInstance.getRepo()
					.getClientLibrary(IProcessClientService.class);
			IProcessArea projectAreas = processClientService.findProcessArea(URI.create(URLEncoder.encode(projectAreaName, "UTF-8").replaceAll("\\+", "%20")), null, teamRepositoryInstance.getMonitor());
			return projectAreas;
		} catch (UnsupportedEncodingException e) {
			CustomLogger.logException(e);
		} catch (TeamRepositoryException e) {
			CustomLogger.logException(e);
		}
		return null;
	}
	
	@SuppressWarnings("rawtypes")
	public IProjectArea getProjectAreas(String projectAreaName)
			throws TeamRepositoryException {
		CustomLogger.logMessage("getProjectAreas");
		IProcessItemService service = (IProcessItemService) teamRepositoryInstance.getRepo().getClientLibrary(IProcessItemService.class);
		IProjectArea projectArea = null;
		List areas = service.findAllProjectAreas(IProcessClientService.ALL_PROPERTIES, teamRepositoryInstance.getMonitor());
		for (Object proArea : areas) {
			if (proArea instanceof IProjectArea) {
				projectArea = (IProjectArea) proArea;
				if (projectArea.getName().equals(projectAreaName)) {
					return projectArea;
				}
			}
		}
		return projectArea;
	}
	
	public List<Node> getIterationHierarchyFromSource(IProjectArea sourceProjectArea) throws TeamRepositoryException{
		IDevelopmentLineHandle[] developmentLines = sourceProjectArea.getDevelopmentLines();
		List<Node> lstNode = new ArrayList<>();
		if (developmentLines.length > 0){
			for (IDevelopmentLineHandle iDevelopmentLineHandle : developmentLines){
				IDevelopmentLine developmentLine = (IDevelopmentLine) teamRepositoryInstance.getRepo().itemManager()
						.fetchCompleteItem(iDevelopmentLineHandle, 0, teamRepositoryInstance.getMonitor());
				IterationPair iterationPair = new IterationPair(developmentLine.getName(), developmentLine);
				Node root = new Node(iterationPair);
				IIterationHandle[] iterations = developmentLine.getIterations();
				if (iterations.length > 0){
					for (IIterationHandle iIterationHandle : iterations) {
			            traverseIteration(iIterationHandle, 1, root);
			        }
				}
				lstNode.add(root);
			}
		}
		return lstNode;
	}
	
	public boolean processCopyIterationToTarget(DefaultMutableTreeNode sourceSelectedNode,
			DefaultMutableTreeNode targetSelectedNode, IProjectArea sourceProjectArea, IProjectArea targetProjectArea,
			JTree sourceIterationTree) throws TeamRepositoryException {

		// Traverse up from the selected node to the root
		TreePath[] selectedPaths = sourceIterationTree.getSelectionPaths();
		boolean isCopied = false;
		for (TreePath treePath : selectedPaths) {
			isCopied = false;
			Deque<IterationPair> hierarchy = new LinkedList<>();
			DefaultMutableTreeNode currentSourceNode = (DefaultMutableTreeNode) treePath.getLastPathComponent();
			//DefaultMutableTreeNode currentSourceNode = sourceSelectedNode;
			Node sourceRoot = null;
			if (currentSourceNode != null) {
				Object userObject = currentSourceNode.getUserObject();
				if (userObject instanceof IterationPair) {
					IterationPair currentSprint = (IterationPair) userObject;
					hierarchy.add(currentSprint);
//					currentSourceNode = null;
				} else if (userObject instanceof String){
//					currentSourceNode = null;
					CustomLogger.logMessage("User Selected Development Timeline for Copy. Cannot copy timeline");
					Notification.addMessage(Status.ERROR.toString(),
							"Cannot copy timeline. Please select Iterations to copy");
					return false;
				}
				
				/* COMMENTED BELOW ONLY TO GET SELECTED ITERATION AND IT'S CHILD. ALSO ADDED  [currentNode = null;] LINE ABOVE */
//				currentNode = (DefaultMutableTreeNode) currentNode.getParent();
				
			}

			List<IterationPair> elements = extractDequeToList(hierarchy);
			
			sourceRoot = createHierarchyToCopy(currentSourceNode, hierarchy, sourceRoot, elements);
			if (sourceRoot != null) {
				printIterationHierarchy(sourceRoot, 0);
				DefaultMutableTreeNode currentTargetNode = targetSelectedNode;
				IterationPair currentTargetIteration = null;
				if (currentTargetNode != null) {
					Object userObject = currentTargetNode.getUserObject();
					if (userObject instanceof IterationPair) {
						currentTargetIteration = (IterationPair) userObject;
					} else if (userObject instanceof String) {
						Notification.addMessage(Status.ERROR.toString(),
								"Please select iteration or a timeline in target project area.");
					}
				}
				if (currentTargetIteration != null) {
					//			IProcessItemService service = (IProcessItemService) teamRepositoryInstance.getRepo().getClientLibrary(IProcessItemService.class);
					Object obj = currentTargetIteration.getObj();
					if (obj instanceof Iteration) {
						Iteration targetIteration = (Iteration) obj;
						IDevelopmentLineHandle developmentLine = targetIteration.getDevelopmentLine();
						IDevelopmentLine targetAreaTimeLine = (IDevelopmentLine) teamRepositoryInstance.getRepo()
								.itemManager().fetchCompleteItem(developmentLine, 0, teamRepositoryInstance.getMonitor());
						boolean copied = createIterationInTargetAreaInSpecificIteration(targetProjectArea, sourceRoot, targetAreaTimeLine,
								targetIteration);
						isCopied = copied;
//						return true;
					} else if (obj instanceof IDevelopmentLine) {
						IDevelopmentLine devLine = (IDevelopmentLine) obj;
//						createIterationInTargetArea(targetProjectArea, sourceRoot, devLine);
						boolean copied = createIterationInTargetAreaInSpecificDevelopmentLine(targetProjectArea, sourceRoot, devLine);
//						CustomLogger.logMessage("User Selected Development Timeline for Copy. Cannot copy timeline");
//						Notification.addMessage(Status.ERROR.toString(),
//								"User Selected Development Timeline for Copy. Cannot copy timeline");
//						return true;
						isCopied = copied;
					}
				} 
			} 
		}
		return isCopied;
	}
	
	public boolean copySpecificIterationFromSource(DefaultMutableTreeNode selectedNode, IProjectArea sourceProjectArea, List<IProjectArea> lstTargetProjectArea) throws TeamRepositoryException{
		
		Deque<IterationPair> hierarchy = new LinkedList<>();
		
     // Traverse up from the selected node to the root
        DefaultMutableTreeNode currentNode = selectedNode;
		Node root = null;
        while (currentNode != null) {
            Object userObject = currentNode.getUserObject();
            if (userObject instanceof IterationPair) {
            	IterationPair currentSprint = (IterationPair) userObject;
                hierarchy.add(currentSprint);
            }
            currentNode = (DefaultMutableTreeNode) currentNode.getParent();
        }
        
        List<IterationPair> elements = extractDequeToList(hierarchy);
        
        root = createHierarchyToCopy(selectedNode, hierarchy, root, elements);
        
        printIterationHierarchy(root, 0);
        
        for (IProjectArea projectArea : lstTargetProjectArea){
        	CustomLogger.logMessage("Fetching development line from Target Project area [" + projectArea.getName() + "]");
				IDevelopmentLine timeLine = createDevelopmentLine(projectArea, root);
				if (timeLine != null) {
					IDevelopmentLine targetAreaTimeLine = (IDevelopmentLine) teamRepositoryInstance.getRepo()
							.itemManager()
							.fetchCompleteItem(timeLine.getItemHandle(), 0, teamRepositoryInstance.getMonitor());
					for (Node child : root.getChildren()) {
						IterationPair iteration = child.getIteration();
						if (iteration.getObj() instanceof Iteration)
							createIterationInTargetArea(projectArea, child, targetAreaTimeLine);
					} 
				} else {
					return false;
				}
        }
       
		return true;
	}

	private Node createHierarchyToCopy(DefaultMutableTreeNode selectedNode, Deque<IterationPair> hierarchy, Node root,
			List<IterationPair> elements) throws TeamRepositoryException {
		Node parent = null;
		boolean hasChild = true;
		for (int i = elements.size() - 1; i >= 0; i--) {
        	IterationPair iterationPair = elements.get(i);
			if (root == null){
				root = new Node(iterationPair);
				if(elements.size() == 1){
					storeChildrenInModel(selectedNode, hierarchy, root, iterationPair);
					hasChild = false;
				}
			}
			else {
				Node child = new Node(iterationPair);
				if (hasChild) {
					root.addChild(child);
					hasChild = false;
				}
				else if (parent != null)
					parent.addChild(child);
				parent = child;
				storeChildrenInModel(selectedNode, hierarchy, child, iterationPair);
			}
        }
		return root;
	}

	private List<IterationPair> extractDequeToList(Deque<IterationPair> hierarchy) {
		List<IterationPair> elements = new LinkedList<>();
        for (IterationPair element : hierarchy) {
            elements.add(element);
        }
		return elements;
	}

	private void storeChildrenInModel(DefaultMutableTreeNode selectedNode, Deque<IterationPair> hierarchy, Node root,
			IterationPair iterationPair) throws TeamRepositoryException {
		DefaultMutableTreeNode currentNode;
		currentNode = selectedNode;
		if (currentNode != null){
			Object userObject = currentNode.getUserObject();
			if (userObject instanceof IterationPair){
				IterationPair selectedIteration = (IterationPair) userObject;
				if(selectedIteration.getDisplay().equals(iterationPair.getDisplay())){
					getChildrenOfSelectedIteration(hierarchy, root);
				}
			}
		}
	}

	private void getChildrenOfSelectedIteration(Deque<IterationPair> hierarchy, Node root) throws TeamRepositoryException{
		if(hierarchy.size() > 0){
			IterationPair iterationPair = hierarchy.peek();
			Object obj = iterationPair.getObj();
			if(obj instanceof Iteration){
				Iteration iteration = (Iteration) obj;
				IIterationHandle[] children = iteration.getChildren();
				if(children.length > 0){
					for (IIterationHandle iIterationHandle : children) {
						traverseIteration(iIterationHandle, 1, root);
					}
				}
			} else if(obj instanceof IDevelopmentLine){
				IDevelopmentLine iteration = (IDevelopmentLine) obj;
				IIterationHandle[] children = iteration.getIterations();
				if(children.length > 0){
					for (IIterationHandle iIterationHandle : children) {
						traverseIteration(iIterationHandle, 1, root);
					}
				}
			}
		}
	}
	
	
	
	// Earlier code to copy all the iteration from source
	public boolean copyIterationHierarchy(IProjectArea sourceProjectArea, List<IProjectArea> lstTargetProjectArea){
		try {
			CustomLogger.logMessage("copyIterationHierarchy");
			CustomLogger.logMessage("Fetching Development line from source project area");
			IDevelopmentLineHandle[] developmentLines = sourceProjectArea.getDevelopmentLines();
			if (developmentLines.length > 0){
//				boolean isDevelopmentLineCreated = false;
				for (IDevelopmentLineHandle iDevelopmentLineHandle : developmentLines){
					IDevelopmentLine developmentLine = (IDevelopmentLine) teamRepositoryInstance.getRepo().itemManager()
							.fetchCompleteItem(iDevelopmentLineHandle, 0, teamRepositoryInstance.getMonitor());
					CustomLogger.logMessage("->" + developmentLine.getName());
					IterationPair iterationPair = new IterationPair(developmentLine.getName(), developmentLine);
					Node root = new Node(iterationPair);
					IIterationHandle[] iterations = developmentLine.getIterations();
					if (iterations.length > 0){
						for (IIterationHandle iIterationHandle : iterations) {
				            traverseIteration(iIterationHandle, 1, root);
				        }
					}
					printIterationHierarchy(root, 0);
					
					for (IProjectArea projectArea : lstTargetProjectArea) {
						CustomLogger.logMessage("Fetching development line from Target Project area [" + projectArea.getName() + "]");
//						IDevelopmentLineHandle[] targetAreaDevelopmentLines = projectArea.getDevelopmentLines();
						
//						if(isDevelopmentLineCreated){
							IDevelopmentLine timeLine = createDevelopmentLine(projectArea, root);
							IDevelopmentLine targetAreaTimeLine = (IDevelopmentLine) teamRepositoryInstance.getRepo().itemManager()
									.fetchCompleteItem(timeLine.getItemHandle(), 0, teamRepositoryInstance.getMonitor());
							for (Node child : root.getChildren()){
								IterationPair iteration = child.getIteration();
								if (iteration.getObj() instanceof Iteration) 
									createIterationInTargetArea(projectArea, child, targetAreaTimeLine);
							}
//						} else {
//							if(targetAreaDevelopmentLines.length > 0){
//								for (IDevelopmentLineHandle targetAreaDevelopmentHandle : targetAreaDevelopmentLines) {
//									IDevelopmentLine targetAreaDevelopmentLine = (IDevelopmentLine) teamRepositoryInstance.getRepo().itemManager()
//											.fetchCompleteItem(targetAreaDevelopmentHandle, 0, teamRepositoryInstance.getMonitor());
//									for (Node child : root.getChildren()){
//										IterationPair iteration = child.getIteration();
//										if (iteration.getObj() instanceof Iteration) 
//											createIterationInTargetArea(projectArea, child, targetAreaDevelopmentLine);
//									}
//									isDevelopmentLineCreated = true;
//									break;
//								}
//							}
//						}
					}
				}
			} else {
				Notification.addMessage(Status.ERROR.toString(),
						"Development line not found!");
				return false;
			}
			return true;
		} catch (Exception e) {
			CustomLogger.logException(e);
			return false;
		}
	}
	
	private IDevelopmentLine createDevelopmentLine(IProjectArea projectArea, Node root) throws TeamRepositoryException{
		IterationPair iterationPair = root.getIteration();
		IDevelopmentLine developmentLineObj = (IDevelopmentLine) iterationPair.getObj();
		
		IDevelopmentLineHandle[] developmentLines = projectArea.getDevelopmentLines();
		int developmentLineCounter = 0;
		if (developmentLines.length > 0){
			for (IDevelopmentLineHandle iDevelopmentLineHandle : developmentLines){
				IDevelopmentLine developmentLine = (IDevelopmentLine) teamRepositoryInstance.getRepo().itemManager()
						.fetchCompleteItem(iDevelopmentLineHandle, 0, teamRepositoryInstance.getMonitor());
				if(!developmentLine.isArchived())
					developmentLineCounter++;
			}
			if(developmentLineCounter > 1){
				Notification.addMessage(Status.ERROR.toString(),
						"More than 1 timeline are present in target project area. Iteration copy skipped!");
				CustomLogger.logMessage("More than 1 timeline are present in target project area. Iteration copy skipped!");
				return null;
			}
				
			for (IDevelopmentLineHandle iDevelopmentLineHandle : developmentLines){
				IDevelopmentLine developmentLine = (IDevelopmentLine) teamRepositoryInstance.getRepo().itemManager()
						.fetchCompleteItem(iDevelopmentLineHandle, 0, teamRepositoryInstance.getMonitor());
//				if(developmentLineObj.getName().equals(developmentLine.getName()) && developmentLineObj.getId().equals(developmentLine.getId()))
//					return developmentLine;
				if(!developmentLine.isArchived())
					return developmentLine;
			}
		}
		
		if (developmentLineObj != null) {
			String identifier = developmentLineObj.getId();
			if(checkIfDevelopmentLineIdentifierExist(projectArea, identifier)){
				int counter = 1;
				identifier = identifier + "(" + counter + ")";
				while(true){
					if(checkIfDevelopmentLineIdentifierExist(projectArea, identifier)){
						counter++;
						identifier = identifier + "(" + counter + ")";
					} else {
						break;
					}
				}
			}
			IProcessItemService service = (IProcessItemService) teamRepositoryInstance.getRepo()
					.getClientLibrary(IProcessItemService.class);
			projectArea = (IProjectArea) service.getMutableCopy(projectArea);
			IDevelopmentLine developmentLine = (IDevelopmentLine) IDevelopmentLine.ITEM_TYPE.createItem();
			developmentLine.setId(identifier);
			developmentLine.setName(developmentLineObj.getName());
			developmentLine.setProjectArea(projectArea);
			projectArea.addDevelopmentLine(developmentLine);
			service.save(new IProcessItem[] { projectArea, developmentLine }, teamRepositoryInstance.getMonitor());
			CustomLogger.logMessage("Time Line [" + developmentLine.getName() + "] Created in [" + projectArea.getName() + "]");
	    	Notification.addMessage(Status.SUCCESSFUL.toString(),
					"Time Line [" + developmentLine.getName() + "] Created in [" + projectArea.getName() + "] Area");
			return developmentLine;
		}
		return null;
	}
	
	private boolean checkIfDevelopmentLineIdentifierExist(IProjectArea projectArea, String identifier) throws TeamRepositoryException{
		IDevelopmentLineHandle[] developmentLines = projectArea.getDevelopmentLines();
		if (developmentLines.length > 0){
			for (IDevelopmentLineHandle iDevelopmentLineHandle : developmentLines){
				IDevelopmentLine developmentLine = (IDevelopmentLine) teamRepositoryInstance.getRepo().itemManager()
						.fetchCompleteItem(iDevelopmentLineHandle, 0, teamRepositoryInstance.getMonitor());
				if(developmentLine.getId().equals(identifier))
					return true;
			}
		}
		return false;
	}
	
	private Iteration checkIfIterationIdentifierExist(IProjectArea projectArea, String identifier) throws TeamRepositoryException{
		IDevelopmentLineHandle[] developmentLines = projectArea.getDevelopmentLines();
		Iteration traverseIteration1 = null;
		if (developmentLines.length > 0){
			for (IDevelopmentLineHandle iDevelopmentLineHandle : developmentLines){
				IDevelopmentLine developmentLine = (IDevelopmentLine) teamRepositoryInstance.getRepo().itemManager()
						.fetchCompleteItem(iDevelopmentLineHandle, 0, teamRepositoryInstance.getMonitor());
//				if(developmentLine.getId().equals(identifier)){
					IIterationHandle[] iterations = developmentLine.getIterations();
					if(iterations.length > 0){
						for (IIterationHandle iIterationHandle : iterations) {
							traverseIteration1 = traverseIteration1(iIterationHandle, identifier);
							if(traverseIteration1 != null)
								return traverseIteration1;
						}
					}
//				}
			}
		}
		return null;
	}
	
	private Iteration traverseIteration1(IIterationHandle iterationHandle, String identifier) throws TeamRepositoryException {
        Iteration iteration = (Iteration) teamRepositoryInstance.getRepo().itemManager()
                .fetchCompleteItem(iterationHandle, 0, teamRepositoryInstance.getMonitor());
        if(iteration.getId().equals(identifier))
        	return iteration;
        if (!iteration.isArchived()) {
//			IterationPair iterationPair = new IterationPair(iteration.getName(), iteration);
			@SuppressWarnings("rawtypes")
			List internalChildren = iteration.getInternalChildren();
			if (internalChildren.size() > 0) {
				for (Object object : internalChildren) {
					if (object instanceof IIterationHandle) {
						IIterationHandle childIterationHandle = (IIterationHandle) object;
						Iteration traverseIteration1 = traverseIteration1(childIterationHandle, identifier);
						if(traverseIteration1 != null)
							return traverseIteration1;
					}
				}
			} 
		}
        return null;
    }
	
	@SuppressWarnings("unused")
	private void createIterationInTargetAreaInTimeLine(IProjectArea projectArea, Node child, IDevelopmentLine targetAreaDevelopmentLine) throws TeamRepositoryException {
		IterationPair iterationPair = child.getIteration();
		Iteration iteration = (Iteration) iterationPair.getObj();
		IProcessItemService service = (IProcessItemService) teamRepositoryInstance.getRepo().getClientLibrary(IProcessItemService.class);
		
		IIteration newIteration = (IIteration) IIteration.ITEM_TYPE.createItem();
		newIteration.setName(iteration.getName());
		newIteration.setId(iteration.getId());
		newIteration.setStartDate(iteration.getStartDate());
		newIteration.setEndDate(iteration.getEndDate());
		newIteration.setHasDeliverable(iteration.hasDeliverable());
		IDevelopmentLine workingDevLine = (IDevelopmentLine) targetAreaDevelopmentLine.getWorkingCopy();
		workingDevLine.addIteration((IIterationHandle) newIteration.getItemHandle());
		newIteration.setDevelopmentLine(workingDevLine);
		
		Iteration checkIfIterationIdentifierExist = checkIfIterationIdentifierExist(projectArea, iteration.getId());
		IProcessItem[] save = null;
		if(checkIfIterationIdentifierExist == null) {
			save = service.save(new IProcessItem[] { workingDevLine, newIteration }, teamRepositoryInstance.getMonitor());
		} else
			newIteration = checkIfIterationIdentifierExist;
		CustomLogger.logMessage("Iteration [" + newIteration.getName() + "] Created in [" + projectArea.getName() + "]");
		Notification.addMessage(Status.SUCCESSFUL.toString(),
				"Iteration [" + newIteration.getName() + "] Created in [" + projectArea.getName() + "] Area");
		
		if(save != null && save.length == 3){
			if(save[2] instanceof IIteration){
				newIteration = (IIteration) save[2];
				createChildIterations(service, child, newIteration, targetAreaDevelopmentLine);
			}
		}
	}
	
	private boolean createIterationInTargetAreaInSpecificIteration(IProjectArea projectArea, Node child, IDevelopmentLine targetAreaDevelopmentLine, IIteration parentIteration) throws TeamRepositoryException {
		Iteration freshParentIterationCopy = getFreshIterationCopy(targetAreaDevelopmentLine, parentIteration);
		parentIteration = (Iteration) freshParentIterationCopy.getWorkingCopy();
		IterationPair iterationPair = child.getIteration();
		Iteration iteration = (Iteration) iterationPair.getObj();
		IProcessItemService service = (IProcessItemService) teamRepositoryInstance.getRepo().getClientLibrary(IProcessItemService.class);
		
		IIteration newIteration = (IIteration) IIteration.ITEM_TYPE.createItem();
		newIteration.setName(iteration.getName());
		newIteration.setId(iteration.getId());
		newIteration.setStartDate(iteration.getStartDate());
		newIteration.setEndDate(iteration.getEndDate());
		newIteration.setHasDeliverable(iteration.hasDeliverable());
		IDevelopmentLine workingDevLine = (IDevelopmentLine) targetAreaDevelopmentLine.getWorkingCopy();
//		workingDevLine.addIteration((IIterationHandle) newIteration.getItemHandle());
		parentIteration.addChild((IIterationHandle) newIteration.getItemHandle());
		newIteration.setParent((IIterationHandle) parentIteration.getItemHandle());
		newIteration.setDevelopmentLine(workingDevLine);
		
		
		Iteration checkIfIterationIdentifierExist = checkIfIterationIdentifierExist(projectArea, iteration.getId());
		IProcessItem[] save = null;
		if(checkIfIterationIdentifierExist == null) {
			save = service.save(new IProcessItem[] { workingDevLine, parentIteration, newIteration }, teamRepositoryInstance.getMonitor());
			
			CustomLogger.logMessage("Iteration [" + newIteration.getName() + "] Created in [" + projectArea.getName() + "]");
			Notification.addMessage(Status.SUCCESSFUL.toString(),
					"Iteration [" + newIteration.getName() + "] Created in [" + projectArea.getName() + "] Area");
			
		} else {
//			newIteration = checkIfIterationIdentifierExist;
			CustomLogger.logMessage("Iteration [" + newIteration.getName() + "] already exist in [" + projectArea.getName() + "]");
			Notification.addMessage(Status.ERROR.toString(),
					"Iteration [" + newIteration.getName() + "] already exist in [" + projectArea.getName() + "] Area");
			return false;
		}
		
		
		if(save != null && save.length == 3){
			if(save[2] instanceof IIteration){
				newIteration = (IIteration) save[2];
				createChildIterations(service, child, newIteration, targetAreaDevelopmentLine);
			}
		}
		return true;
	}
	
	private boolean createIterationInTargetAreaInSpecificDevelopmentLine(IProjectArea projectArea, Node child, IDevelopmentLine targetAreaDevelopmentLine) throws TeamRepositoryException {
//		Iteration freshParentIterationCopy = getFreshIterationCopy(targetAreaDevelopmentLine, parentIteration);
//		parentIteration = (Iteration) freshParentIterationCopy.getWorkingCopy();
		IterationPair iterationPair = child.getIteration();
		Iteration iteration = (Iteration) iterationPair.getObj();
		IProcessItemService service = (IProcessItemService) teamRepositoryInstance.getRepo().getClientLibrary(IProcessItemService.class);
		
		IIteration newIteration = (IIteration) IIteration.ITEM_TYPE.createItem();
		newIteration.setName(iteration.getName());
		newIteration.setId(iteration.getId());
		newIteration.setStartDate(iteration.getStartDate());
		newIteration.setEndDate(iteration.getEndDate());
		newIteration.setHasDeliverable(iteration.hasDeliverable());
		IDevelopmentLine workingDevLine = (IDevelopmentLine) targetAreaDevelopmentLine.getWorkingCopy();
		workingDevLine.addIteration((IIterationHandle) newIteration.getItemHandle());
//		parentIteration.addChild((IIterationHandle) newIteration.getItemHandle());
//		newIteration.setParent((IIterationHandle) parentIteration.getItemHandle());
		newIteration.setDevelopmentLine(workingDevLine);
		
		
		Iteration checkIfIterationIdentifierExist = checkIfIterationIdentifierExist(projectArea, iteration.getId());
		IProcessItem[] save = null;
		if(checkIfIterationIdentifierExist == null) {
			save = service.save(new IProcessItem[] { workingDevLine, newIteration }, teamRepositoryInstance.getMonitor());
			
			CustomLogger.logMessage("Iteration [" + newIteration.getName() + "] Created in [" + projectArea.getName() + "]");
			Notification.addMessage(Status.SUCCESSFUL.toString(),
					"Iteration [" + newIteration.getName() + "] Created in [" + projectArea.getName() + "] Area");
			
		} else {
//			newIteration = checkIfIterationIdentifierExist;
			CustomLogger.logMessage("Iteration [" + newIteration.getName() + "] already exist in [" + projectArea.getName() + "]");
			Notification.addMessage(Status.ERROR.toString(),
					"Iteration [" + newIteration.getName() + "] already exist in [" + projectArea.getName() + "] Area");
			return false;
		}
		
		
		if(save != null && save.length == 2){
			if(save[1] instanceof IIteration){
				newIteration = (IIteration) save[1];
				createChildIterations(service, child, newIteration, targetAreaDevelopmentLine);
			}
		}
		return true;
	}
	
	private void createIterationInTargetArea(IProjectArea projectArea, Node child, IDevelopmentLine targetAreaDevelopmentLine) throws TeamRepositoryException {
		IterationPair iterationPair = child.getIteration();
		Iteration iteration = (Iteration) iterationPair.getObj();
		IProcessItemService service = (IProcessItemService) teamRepositoryInstance.getRepo().getClientLibrary(IProcessItemService.class);
		IIteration newIteration = (IIteration) IIteration.ITEM_TYPE.createItem();
		newIteration.setName(iteration.getName());
		newIteration.setId(iteration.getId());
		newIteration.setStartDate(iteration.getStartDate());
		newIteration.setEndDate(iteration.getEndDate());
		newIteration.setHasDeliverable(iteration.hasDeliverable());
		IDevelopmentLine workingDevLine = (IDevelopmentLine) targetAreaDevelopmentLine.getWorkingCopy();
		workingDevLine.addIteration((IIterationHandle) newIteration.getItemHandle());
		newIteration.setDevelopmentLine(workingDevLine);
		
		Iteration checkIfIterationIdentifierExist = checkIfIterationIdentifierExist(projectArea, iteration.getId());
		if(checkIfIterationIdentifierExist == null)
			service.save(new IProcessItem[] { workingDevLine, newIteration }, teamRepositoryInstance.getMonitor());
		else
			newIteration = checkIfIterationIdentifierExist;
		CustomLogger.logMessage("Iteration [" + newIteration.getName() + "] Created in [" + projectArea.getName() + "]");
		Notification.addMessage(Status.SUCCESSFUL.toString(),
				"Iteration [" + newIteration.getName() + "] Created in [" + projectArea.getName() + "] Area");
		
		IIterationHandle[] iterations = targetAreaDevelopmentLine.getIterations();
		if(iterations.length > 0){
			for (IIterationHandle iIterationHandle : iterations){
				Iteration childIteration = (Iteration) teamRepositoryInstance.getRepo().itemManager()
						.fetchCompleteItem(iIterationHandle, 0, teamRepositoryInstance.getMonitor());
				if(childIteration.getName().equals(newIteration.getName())){
					createChildIterations(service, child, childIteration, targetAreaDevelopmentLine);
				}
			}
		}
	}
	
	private void createChildIterations(IProcessItemService service, Node parentNode, IIteration parentIteration, IDevelopmentLine targetAreaDevelopmentLine) throws TeamRepositoryException {		
		IDevelopmentLine workingDevLine = (IDevelopmentLine) targetAreaDevelopmentLine.getWorkingCopy();
		List<Node> children = parentNode.getChildren();
	    for (Node node : children) {
	    	Iteration freshIterationCopy = getFreshIterationCopy(targetAreaDevelopmentLine, parentIteration);
	    	if (freshIterationCopy != null) {
				parentIteration = (Iteration) freshIterationCopy.getWorkingCopy();
				workingDevLine = (IDevelopmentLine) targetAreaDevelopmentLine.getWorkingCopy();
				IterationPair childIterationPair = node.getIteration();
				Object obj = childIterationPair.getObj();
				if (obj instanceof Iteration) {
					IIteration newChildIteration = (IIteration) IIteration.ITEM_TYPE.createItem();
					newChildIteration.setName(((Iteration) obj).getName());
					newChildIteration.setId(((Iteration) obj).getId());
					newChildIteration.setStartDate(((Iteration) obj).getStartDate());
					newChildIteration.setEndDate(((Iteration) obj).getEndDate());
					newChildIteration.setHasDeliverable(((Iteration) obj).hasDeliverable());
					parentIteration.addChild((IIterationHandle) newChildIteration.getItemHandle());
					newChildIteration.setParent((IIterationHandle) parentIteration.getItemHandle());
					newChildIteration.setDevelopmentLine(workingDevLine);

					service.save(new IProcessItem[] { newChildIteration, parentIteration, workingDevLine },
							teamRepositoryInstance.getMonitor());
					CustomLogger.logMessage("Child Iteration Created [" + newChildIteration.getName() + "] for parent Iteration [" + parentIteration.getName() + "]");
					Notification.addMessage(Status.SUCCESSFUL.toString(),
							"Child Iteration Created [" + newChildIteration.getName() + "] for parent Iteration [" + parentIteration.getName() + "]");
					
					// Recursive call to create children for the current child iteration
					if (node.getChildren().size() > 0)
						createChildIterations(service, node, newChildIteration, workingDevLine);
				} 
			}
	    }
	}
	
	private Iteration getFreshIterationCopy(IDevelopmentLine targetAreaDevelopmentLine, IIteration oldIteration) throws TeamRepositoryException{
		CustomLogger.logMessage("Getting fresh copy of Iteration");
		IIterationHandle[] iterations = targetAreaDevelopmentLine.getIterations();
		if(iterations.length > 0){
			for (IIterationHandle iIterationHandle : iterations) {
				Iteration childIteration = (Iteration) teamRepositoryInstance.getRepo().itemManager()
    	                .fetchCompleteItem(iIterationHandle, 0, teamRepositoryInstance.getMonitor());
				if(childIteration.getItemId().getUuidValue().equals(oldIteration.getItemId().getUuidValue()))
					return childIteration;
				
				Iteration matchingChild = findMatchingChildIteration(childIteration, oldIteration);
	            if (matchingChild != null) {
	                return matchingChild;
	            }
			}
		}
		
		return null;
	}
	
	private Iteration findMatchingChildIteration(IIteration parentIteration, IIteration oldIteration)
	        throws TeamRepositoryException {
	    IIterationHandle[] children = parentIteration.getChildren();
	    for (IIterationHandle iIterationHandle : children) {
	        Iteration childIteration = fetchCompleteIteration(iIterationHandle);

	        // Check if the current child iteration matches the oldIteration
	        if (childIteration.getItemId().getUuidValue().equals(oldIteration.getItemId().getUuidValue())) {
	            return childIteration;
	        }

	        // Recursively check children
	        Iteration matchingChild = findMatchingChildIteration(childIteration, oldIteration);
	        if (matchingChild != null) {
	            return matchingChild;
	        }
	    }
	    return null;
	}

	private Iteration fetchCompleteIteration(IIterationHandle iterationHandle) throws TeamRepositoryException {
	    return (Iteration) teamRepositoryInstance.getRepo().itemManager()
	            .fetchCompleteItem(iterationHandle, 0, teamRepositoryInstance.getMonitor());
	}
	
	private void printIterationHierarchy(Node node, int depth) {
        StringBuilder indent = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            indent.append("  ");
        }
//        System.out.println(indent + "-> " + node);
        CustomLogger.logMessage(indent + "-> " + node);

        for (Node child : node.getChildren()) {
            printIterationHierarchy(child, depth + 1);
        }
    }
	
	@SuppressWarnings("rawtypes")
	private void traverseIteration(IIterationHandle iterationHandle, int depth, Node root) throws TeamRepositoryException {
        Iteration iteration = (Iteration) teamRepositoryInstance.getRepo().itemManager()
                .fetchCompleteItem(iterationHandle, 0, teamRepositoryInstance.getMonitor());

        if (!iteration.isArchived()) {
			IterationPair iterationPair = new IterationPair(iteration.getName(), iteration);
			Node iterationNode = new Node(iterationPair);
			root.addChild(iterationNode);
			List internalChildren = iteration.getInternalChildren();
			if (internalChildren.size() > 0) {
				for (Object object : internalChildren) {
					if (object instanceof IIterationHandle) {
						IIterationHandle childIterationHandle = (IIterationHandle) object;
						traverseIteration(childIterationHandle, depth + 1, iterationNode);
					}
				}
			} 
		}
    }
	
}
