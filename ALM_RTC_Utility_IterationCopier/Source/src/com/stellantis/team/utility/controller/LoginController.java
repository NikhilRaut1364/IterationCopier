package com.stellantis.team.utility.controller;

import org.eclipse.core.runtime.IProgressMonitor;

import com.ibm.team.repository.client.ITeamRepository;
import com.ibm.team.repository.client.TeamPlatform;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.stellantis.team.utility.model.TeamRepositoryInstance;
import com.stellantis.team.utility.utils.CustomLogger;

public class LoginController{
	public boolean login(String repoAddress, final String userId, final String password,
			IProgressMonitor monitor) {
		try {
			TeamRepositoryInstance teamRepositoryInstance = TeamRepositoryInstance.getInstance();
			ITeamRepository repository = TeamPlatform.getTeamRepositoryService().getTeamRepository(repoAddress);
			repository.registerLoginHandler(new ITeamRepository.ILoginHandler() {
				public ILoginInfo challenge(ITeamRepository repository) {
					return new ILoginInfo() {
						public String getUserId() {
							return userId;
						}

						public String getPassword() {
							return password;
						}
					};
				}
			});
			monitor.subTask("Contacting " + repository.getRepositoryURI() + "...");
			repository.login(monitor);
			teamRepositoryInstance.setRepo(repository);
			teamRepositoryInstance.setMonitor(monitor);
			teamRepositoryInstance.setUsername(userId);
			teamRepositoryInstance.setPassword(password);
			teamRepositoryInstance.setServerURL(repoAddress);
			monitor.subTask("Connected");
		} catch (TeamRepositoryException e) {
			CustomLogger.logException(e);
		}
		return true;
	}
	
	public boolean logout(){
		try {
			TeamRepositoryInstance teamRepositoryInstance = TeamRepositoryInstance.getInstance();
			ITeamRepository repo = teamRepositoryInstance.getRepo();
			if(repo != null){
				repo.logout();
				return true;
			}
		} catch (Exception e) {
			CustomLogger.logException(e);
		}
		return false;
	}
}
