package com.stellantis.team.utility.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

import com.stellantis.team.utility.controller.LoginController;
import com.stellantis.team.utility.model.IterationPair;
import com.stellantis.team.utility.model.KeyValuePair;
import com.stellantis.team.utility.model.Node;
import com.stellantis.team.utility.model.ProjectAreaPair;
import com.stellantis.team.utility.model.Status;
import com.stellantis.team.utility.service.CopyWorker;
import com.stellantis.team.utility.service.IterationHierarchyWorker;
import com.stellantis.team.utility.service.LoginWorker;
import com.stellantis.team.utility.utils.CommonUtils;
import com.stellantis.team.utility.utils.CustomLogger;
import com.stellantis.team.utility.utils.ExtractProperties;
import com.stellantis.team.utility.utils.UtilityConstants;

@SuppressWarnings("serial")
public class IterationCopier extends JFrame {
	private static final String IMAGES_FCA_PNG = "/images/fca.png";
	private static final String CLOSE = "Close";
	private static final String TARGET_PROJECT_AREA = "Target Project Areas ";
	private static final String SOURCE_PROJECT_AREA = "Source Project Areas ";
	private static final String COPY = "Copy";
	private static final String COPY_HEADER = "Copy [Please select a source project iteration and a target project iteration or a timeline]";
	private JPanel contentPane;
	private JTextField txtUsername;
	private JPasswordField txtPassword;
	private JPanel pnlLogin;
	private JComboBox<KeyValuePair> cmbServer;
	private JButton btnLoginLogout;
	private JLabel lblLogs;
	private JLabel lblSourceProject;
	private JComboBox<ProjectAreaPair> cmbSourceProjectArea;
	private JLabel lblTargetProjectArea;
	private JScrollPane scrTargetProjectArea;
	private JList<ProjectAreaPair> lstTargetProjectArea;
	private DefaultListModel<ProjectAreaPair> lstTargetProjectAreaModel = new DefaultListModel<>();
	private JButton btnCopy;
	private JButton btnClose;
	private JScrollPane scrNotification;
	private Notification tblNotification;
	private JPanel pnlIterationTree;
	private JTree sourceIterationTree;
	private JComboBox<ProjectAreaPair> cmbTargetProjectArea;
	private JTree targetIterationTree;
	private static final int TEXT_FIELD_WIDTH = 160;
	private static final int TEXT_FIELD_HEIGHT = 35;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ExtractProperties.getInstance();
					IterationCopier frame = new IterationCopier();
					frame.setVisible(true);
				} catch (Exception e) {
					CustomLogger.logException(e);
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public IterationCopier() {
		CustomLogger.logMessage("IterationCopier");
		Dimension size = new Dimension(TEXT_FIELD_WIDTH, TEXT_FIELD_HEIGHT);
		Font italicFont = new Font(UtilityConstants.FONT_NAME, Font.ITALIC, 18);
		CommonUtils.setUIFont(new javax.swing.plaf.FontUIResource(UtilityConstants.FONT_NAME, Font.PLAIN, 18));
		Image image = Toolkit.getDefaultToolkit().getImage(getClass().getResource(IMAGES_FCA_PNG));
		setIconImage(image);
//		setResizable(false);
		setTitle(UtilityConstants.UTILITY_NAME + " - " + UtilityConstants.UTILITY_VERSION);
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1342, 778);
		contentPane = new JPanel();
		contentPane.setBackground(Color.WHITE);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));
		
		pnlLogin = new JPanel();
		pnlLogin.setBackground(Color.WHITE);
		pnlLogin.setBorder(new TitledBorder(null, UtilityConstants.LOGIN, TitledBorder.LEADING, TitledBorder.TOP, null, null));
		contentPane.add(pnlLogin, BorderLayout.NORTH);
		GridBagLayout gbl_pnlLogin = new GridBagLayout();
		gbl_pnlLogin.columnWidths = new int[] {0, 0, 0, 0, 30, 0, 30};
		gbl_pnlLogin.rowHeights = new int[]{0, 0};
		gbl_pnlLogin.columnWeights = new double[]{1.0, 1.0, 1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_pnlLogin.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlLogin.setLayout(gbl_pnlLogin);
		
		cmbServer = new JComboBox<KeyValuePair>();
		cmbServer.setFont(italicFont);
		bindServerComboBox(cmbServer);
		GridBagConstraints gbc_cmbServer = new GridBagConstraints();
		gbc_cmbServer.insets = new Insets(0, 0, 0, 5);
		gbc_cmbServer.fill = GridBagConstraints.HORIZONTAL;
		gbc_cmbServer.gridx = 0;
		gbc_cmbServer.gridy = 0;
		pnlLogin.add(cmbServer, gbc_cmbServer);
		
		txtUsername = new JTextField();
		txtUsername.setFont(italicFont);
		txtUsername.setPreferredSize(size);
		GridBagConstraints gbc_txtUsername = new GridBagConstraints();
		gbc_txtUsername.insets = new Insets(0, 0, 0, 5);
		gbc_txtUsername.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtUsername.gridx = 1;
		gbc_txtUsername.gridy = 0;
		pnlLogin.add(txtUsername, gbc_txtUsername);
		txtUsername.setColumns(10);
		txtUsername.setText(UtilityConstants.USERNAME);
		txtUsername.addFocusListener(new FocusAdapter() {

			@Override
			public void focusGained(FocusEvent e) {
				if (txtUsername.getText().equals(UtilityConstants.USERNAME)) {
					txtUsername.setText(UtilityConstants.CONSTANT_NO_VALUE);
				}
			}

			@Override
			public void focusLost(FocusEvent e) {
				if (txtUsername.getText().equals(UtilityConstants.CONSTANT_NO_VALUE)) {
					txtUsername.setText(UtilityConstants.USERNAME);
				}
			}
			
		});
		
		txtPassword = new JPasswordField();
		txtPassword.setFont(italicFont);
		txtPassword.setPreferredSize(size);
		GridBagConstraints gbc_txtPassword = new GridBagConstraints();
		gbc_txtPassword.insets = new Insets(0, 0, 0, 5);
		gbc_txtPassword.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtPassword.gridx = 2;
		gbc_txtPassword.gridy = 0;
		pnlLogin.add(txtPassword, gbc_txtPassword);
		txtPassword.setText(UtilityConstants.PASSWORD);
		txtPassword.setEchoChar((char)0);
		txtPassword.addFocusListener(new FocusAdapter() {

			@SuppressWarnings("deprecation")
			@Override
			public void focusGained(FocusEvent e) {
				if (txtPassword.getText().equals(UtilityConstants.PASSWORD)) {
					txtPassword.setEchoChar('*');
					txtPassword.setText(UtilityConstants.CONSTANT_NO_VALUE);
				}
			}

			@SuppressWarnings("deprecation")
			@Override
			public void focusLost(FocusEvent e) {
				if (txtPassword.getText().equals(UtilityConstants.CONSTANT_NO_VALUE)) {
					txtPassword.setEchoChar((char)0);
					txtPassword.setText(UtilityConstants.PASSWORD);
				} else {
					txtPassword.setEchoChar('*');
				}
			}
			
		});
		
		btnLoginLogout = new JButton("Login");
		btnLoginLogout.setBackground(Color.LIGHT_GRAY);
		CommonUtils.setButtonFontToBold(btnLoginLogout);
		CommonUtils.setButtonFontToBold(btnLoginLogout);
		GridBagConstraints gbc_btnLoginLogout = new GridBagConstraints();
		gbc_btnLoginLogout.insets = new Insets(0, 0, 0, 5);
		gbc_btnLoginLogout.gridx = 3;
		gbc_btnLoginLogout.gridy = 0;
		pnlLogin.add(btnLoginLogout, gbc_btnLoginLogout);
		componentActionHandler(btnLoginLogout);
		
		lblLogs = new JLabel(UtilityConstants.LOGS);
		GridBagConstraints gbc_lblLogs = new GridBagConstraints();
		gbc_lblLogs.gridx = 5;
		gbc_lblLogs.gridy = 0;
		pnlLogin.add(lblLogs, gbc_lblLogs);
		lblLogs.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				lblLogs.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			}

			@Override
			public void mouseExited(MouseEvent e) {
				lblLogs.setCursor(Cursor.getDefaultCursor());
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				CommonUtils.openNotepadFile(UtilityConstants.EXCEPTION_FILE_PATH);
			}
		});
		
		JPanel pnlMain = new JPanel();
		pnlMain.setBackground(Color.WHITE);
		contentPane.add(pnlMain, BorderLayout.CENTER);
		pnlMain.setLayout(new BorderLayout(0, 0));
		
		JPanel pnlProjectArea = new JPanel();
		pnlProjectArea.setBackground(Color.WHITE);
		pnlProjectArea.setBorder(new TitledBorder(null, COPY_HEADER, TitledBorder.LEADING, TitledBorder.TOP, null, null));
		pnlMain.add(pnlProjectArea, BorderLayout.CENTER);
		pnlProjectArea.setLayout(new GridLayout(1, 0, 20, 20));
		
		JPanel pnlSource = new JPanel();
		pnlSource.setBackground(Color.WHITE);
		pnlProjectArea.add(pnlSource);
		pnlSource.setLayout(new BorderLayout(0, 0));
		
		JPanel pnlSourceProject = new JPanel();
		pnlSourceProject.setBackground(Color.WHITE);
		pnlSource.add(pnlSourceProject, BorderLayout.NORTH);
		pnlSourceProject.setLayout(new BoxLayout(pnlSourceProject, BoxLayout.X_AXIS));
		
		lblSourceProject = new JLabel(SOURCE_PROJECT_AREA);
		pnlSourceProject.add(lblSourceProject);
		
		cmbSourceProjectArea = new JComboBox<ProjectAreaPair>();
		pnlSourceProject.add(cmbSourceProjectArea);
		pnlSourceProject.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 100));
		cmbSourceProjectArea.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(cmbSourceProjectArea.getSelectedIndex() > 0){
					setComponentsEnabled(false, cmbSourceProjectArea, btnLoginLogout, btnCopy);
					ProjectAreaPair projectArea = (ProjectAreaPair) cmbSourceProjectArea.getSelectedItem();
					IterationHierarchyWorker hierarchyWorker = new IterationHierarchyWorker(projectArea.getProjectAreaObj()){
						@Override
						protected void done(){
							try {
								boolean isValid = get();
								if (isValid) {
									Notification.addMessage(Status.INFO.toString(), "Please wait! Loading the hierarchy view of Timeline");
									List<Node> iterationHierarchy = getHierarchy();
									DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(projectArea.getProjectAreaObj().getName());
									for (Node node : iterationHierarchy) {
										DefaultMutableTreeNode rootTreeNode = new DefaultMutableTreeNode(new IterationPair(node.getIteration().getDisplay(), node.getIteration().getObj()));
							            rootNode.add(rootTreeNode);
							            buildJTree(node, rootTreeNode);
									}
									sourceIterationTree.setModel(new DefaultTreeModel(rootNode));
								}
								setComponentsEnabled(true, cmbSourceProjectArea, btnLoginLogout, btnCopy);
							} catch (InterruptedException e) {
								CustomLogger.logException(e);
							} catch (ExecutionException e) {
								CustomLogger.logException(e);
							}
						}
					};
					hierarchyWorker.execute();
				}
			}
		});
		
		pnlIterationTree = new JPanel();
		pnlSource.add(pnlIterationTree, BorderLayout.CENTER);
		pnlIterationTree.setLayout(new GridLayout(1, 0, 0, 0));
		
		sourceIterationTree = new JTree(new DefaultMutableTreeNode(""));
		sourceIterationTree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
		JScrollPane treeScrollPane = new JScrollPane(sourceIterationTree);
		pnlIterationTree.add(treeScrollPane);
		
		JPanel pnlTarget = new JPanel();
		pnlTarget.setBackground(Color.WHITE);
		pnlProjectArea.add(pnlTarget);
		pnlTarget.setLayout(new BorderLayout(0, 0));
		
		JPanel pnlTargetProjectArea1 = new JPanel();
		pnlTargetProjectArea1.setBackground(Color.WHITE);
		pnlTarget.add(pnlTargetProjectArea1, BorderLayout.NORTH);
		pnlTargetProjectArea1.setLayout(new BoxLayout(pnlTargetProjectArea1, BoxLayout.X_AXIS));
		
		lblTargetProjectArea = new JLabel(TARGET_PROJECT_AREA);
		pnlTargetProjectArea1.add(lblTargetProjectArea);
		
		cmbTargetProjectArea =  new JComboBox<ProjectAreaPair>();
		pnlTargetProjectArea1.add(cmbTargetProjectArea);
		pnlTargetProjectArea1.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 100));
		cmbTargetProjectArea.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				refreshTargetProjectIterationsView();
			}
		});
		
		JPanel pnlTargetIterationTree = new JPanel();
		pnlTargetIterationTree.setBackground(Color.WHITE);
		pnlTarget.add(pnlTargetIterationTree, BorderLayout.CENTER);
		pnlTargetIterationTree.setLayout(new GridLayout(0, 1, 0, 0));
		
		targetIterationTree = new JTree(new DefaultMutableTreeNode(""));
		targetIterationTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		JScrollPane targetTreeScrollPane = new JScrollPane(targetIterationTree);
		pnlTargetIterationTree.add(targetTreeScrollPane);
		
		JPanel pnlTargetProjectArea = new JPanel();
		pnlTargetProjectArea.setBackground(Color.WHITE);
//		pnlTarget.add(pnlTargetProjectArea, BorderLayout.CENTER);
		pnlTargetProjectArea.setLayout(new BorderLayout(5, 2));
		
		lblTargetProjectArea = new JLabel(TARGET_PROJECT_AREA);
		pnlTargetProjectArea.add(lblTargetProjectArea, BorderLayout.NORTH);
		
		lstTargetProjectArea = new JList<ProjectAreaPair>(lstTargetProjectAreaModel);
		lstTargetProjectArea.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		scrTargetProjectArea = new JScrollPane(lstTargetProjectArea);
		pnlTargetProjectArea.add(scrTargetProjectArea, BorderLayout.CENTER);
		
		
		JPanel pnlButtons = new JPanel();
		pnlButtons.setBackground(Color.WHITE);
		pnlMain.add(pnlButtons, BorderLayout.SOUTH);
		pnlButtons.setLayout(new BorderLayout(0, 0));
		
		JPanel pnlBtn = new JPanel();
		pnlBtn.setBackground(Color.WHITE);
		pnlButtons.add(pnlBtn, BorderLayout.EAST);
		pnlBtn.setLayout(new BoxLayout(pnlBtn, BoxLayout.X_AXIS));
		
		btnCopy = new JButton(COPY);
		btnCopy.setBackground(Color.LIGHT_GRAY);
		CommonUtils.setButtonFontToBold(btnCopy);
		pnlBtn.add(btnCopy);
		componentActionHandler(btnCopy);
		
		btnClose = new JButton(CLOSE);
		btnClose.setBackground(Color.LIGHT_GRAY);
		CommonUtils.setButtonFontToBold(btnClose);
		btnClose.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	LoginController loginController = new LoginController();
				loginController.logout();
                dispose(); 
            }
        });
		pnlBtn.add(btnClose);
		
		JPanel pnlNote = new JPanel();
		pnlButtons.add(pnlNote, BorderLayout.CENTER);
		pnlNote.setBackground(Color.WHITE);
		pnlNote.setLayout(new GridLayout(0, 1, 0, 0));
		
		JTextArea lblImpNote = new JTextArea(UtilityConstants.IMP_NOTE);
		lblImpNote.setLineWrap(true);  
		lblImpNote.setWrapStyleWord(true);  
		lblImpNote.setEditable(false); 
		lblImpNote.setOpaque(false);  
		lblImpNote.setFocusable(false); 
		lblImpNote.setBorder(BorderFactory.createEmptyBorder());
		lblImpNote.setBackground(Color.WHITE);
		lblImpNote.setForeground(Color.RED);
//		pnlNote.add(lblImpNote);
		
		JPanel pnlNotification = new JPanel();
		pnlNotification.setBackground(Color.WHITE);
		contentPane.add(pnlNotification, BorderLayout.SOUTH);
		pnlNotification.setLayout(new GridLayout(1, 0, 0, 0));
		
		tblNotification = new Notification();
		tblNotification.setBackground(Color.WHITE);
		
		scrNotification = new JScrollPane(tblNotification);
		scrNotification.setPreferredSize(new Dimension(600, 350));
		pnlNotification.add(scrNotification);
		
		setComponentsEnabled(false, cmbSourceProjectArea, cmbTargetProjectArea, lstTargetProjectArea, btnCopy, btnClose);
		getRootPane().setDefaultButton(btnLoginLogout);
		CustomLogger.logMessage("IterationCopier UI Created");
	}
	
	private void bindServerComboBox(JComboBox<KeyValuePair> comboBox) {
		CustomLogger.logMessage("Binding Server combo box");
		String[] parts = ExtractProperties.getServerURLList().split(";");

		for (String part : parts) {
			String[] keyValue = part.split("=");
			if (keyValue.length == 2) {
				comboBox.addItem(new KeyValuePair(keyValue[0], keyValue[1]));
			}
		}
	}

	private void componentActionHandler(JComponent component){
		if(component instanceof JButton){
			if(component.equals(btnLoginLogout)){
				btnLoginLogout.addActionListener(new ActionListener() {
					
					@SuppressWarnings("deprecation")
					@Override
					public void actionPerformed(ActionEvent e) {
						if (btnLoginLogout.getText().equals(UtilityConstants.LOGIN)){
							CustomLogger.logMessage("Button Login Clicked");
							KeyValuePair selectedServer = (KeyValuePair) cmbServer.getSelectedItem();
							String username = txtUsername.getText();
							String password = txtPassword.getText();
							if(username.isEmpty() || password.isEmpty()){
								Notification.addMessage(Status.ERROR.toString(), "Please enter username and password!");
								return;
							}
							setComponentsEnabled(false, cmbServer, txtUsername, txtPassword, btnLoginLogout);
							LoginWorker loginWorker = new LoginWorker(selectedServer, username, password) {
								@Override
								protected void done() {
									try {
										boolean isValid = get();
										if (isValid) {
											List<ProjectAreaPair> allProjectAreas = getFetchAllProjectAreas();
											cmbSourceProjectArea.addItem(new ProjectAreaPair("- SELECT -", null));
											cmbTargetProjectArea.addItem(new ProjectAreaPair("- SELECT -", null));
											for (ProjectAreaPair projectAreaPair : allProjectAreas) {
												cmbSourceProjectArea.addItem(projectAreaPair);
												cmbTargetProjectArea.addItem(projectAreaPair);
												lstTargetProjectAreaModel.addElement(projectAreaPair);
											}
											Notification.addMessage(Status.SUCCESSFUL.toString(),
													"Project area fetched successfully. Please select the project area to proceed.");
											
											setComponentsEnabled(true, btnLoginLogout, cmbSourceProjectArea, cmbTargetProjectArea, lstTargetProjectArea, btnCopy, btnClose);
											btnLoginLogout.setText(UtilityConstants.LOGOUT);
										} else {
											setComponentsEnabled(true, cmbServer, txtUsername, txtPassword, btnLoginLogout);
										}
									} catch (InterruptedException e) {
										CustomLogger.logException(e);
									} catch (ExecutionException e) {
										CustomLogger.logException(e);
									}
								}
							};
							loginWorker.execute();
						} else if (btnLoginLogout.getText().equals(UtilityConstants.LOGOUT)){
							CustomLogger.logMessage("Button Logout Clicked");
							btnLoginLogout.setText(UtilityConstants.LOGIN);
							
							LoginController loginController = new LoginController();
							loginController.logout();
							txtUsername.setText(UtilityConstants.USERNAME);
							txtPassword.setText(UtilityConstants.PASSWORD);
							txtPassword.setEchoChar((char)0);
							
							cmbSourceProjectArea.removeAllItems();
							cmbTargetProjectArea.removeAllItems();
							lstTargetProjectAreaModel.clear();
							setComponentsEnabled(false, cmbSourceProjectArea, cmbTargetProjectArea, lstTargetProjectArea, btnCopy, btnClose);
							setComponentsEnabled(true, cmbServer, txtUsername, txtPassword, btnLoginLogout);
							
							clearIterationTree(sourceIterationTree);
							clearIterationTree(targetIterationTree);
						    
						    System.gc();
						}
					}
				});
			} else if(component.equals(btnCopy)){
				btnCopy.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						CustomLogger.logMessage("Button copy Clicked");
						
						DefaultMutableTreeNode sourceSelectedNode = (DefaultMutableTreeNode) sourceIterationTree.getLastSelectedPathComponent();
						DefaultMutableTreeNode targetSelectedNode = (DefaultMutableTreeNode) targetIterationTree.getLastSelectedPathComponent();

						if (sourceSelectedNode != null && targetSelectedNode != null) {
							ProjectAreaPair sourceProjectAreaSelected = (ProjectAreaPair) cmbSourceProjectArea
									.getSelectedItem();
							
							ProjectAreaPair targetProjectAreaSelected = (ProjectAreaPair) cmbTargetProjectArea
									.getSelectedItem();
							
							/* // COMMENTING BELOW CODE DUE TO CHANGE IN FUNCTIONALITY.
							int[] selectedIndices = lstTargetProjectArea.getSelectedIndices();
							List<IProjectArea> lstSelectedTargetProjectArea = new ArrayList<>();
							List<String> lstTargetNames = new ArrayList<>();
							for (int i : selectedIndices) {
								ProjectAreaPair elementAt = lstTargetProjectAreaModel.getElementAt(i);
								lstTargetNames.add(elementAt.getProjectAreaName());
								lstSelectedTargetProjectArea.add(elementAt.getProjectAreaObj());
							}
							if (lstTargetNames.size() <= 0) {
								Notification.addMessage(Status.INFO.toString(), "Please select target project area!");
								return;
							}
							*/
							
							int choice = JOptionPane.showConfirmDialog(IterationCopier.this,
									"Are you sure you want to copy from Source ["
											+ sourceProjectAreaSelected.getProjectAreaName() + "] to Target ["
											+ String.join(",", targetProjectAreaSelected.getProjectAreaName()) + "] Area?",
									"Confirmation", JOptionPane.YES_NO_OPTION);
							if (choice == JOptionPane.YES_OPTION) {
								CustomLogger.logMessage("User clicked YES for copy operation");
								setComponentsEnabled(false, cmbSourceProjectArea, cmbTargetProjectArea, lstTargetProjectArea, btnCopy,
										btnLoginLogout);

								/* COMMENTED BELOW CODE DUE TO REMOVAL OF [lstSelectedTargetProjectArea] AND ADDITION OF NEW PARAMETERS
								 * CopyWorker copyWorker = new CopyWorker(sourceProjectAreaSelected.getProjectAreaObj(),
										lstSelectedTargetProjectArea, sourceSelectedNode) */
								Notification.addMessage(Status.INFO.toString(),
										"Please wait while iteration gets copy.");
								CopyWorker copyWorker = new CopyWorker(sourceProjectAreaSelected.getProjectAreaObj(), targetProjectAreaSelected.getProjectAreaObj(),
										null, sourceSelectedNode, targetSelectedNode, sourceIterationTree) {
									@Override
									protected void done() {
										try {
											boolean isValid = get();
											if (isValid) {
												lstTargetProjectArea.clearSelection();
												sourceIterationTree.clearSelection();
												targetIterationTree.clearSelection();
												CustomLogger.logMessage("Copy operation complete");
												Notification.addMessage(Status.SUCCESSFUL.toString(),
														"Iteration copied successfully");
												
												
												refreshTargetProjectIterationsView();											
												
												
												
												
											} else {
												CustomLogger.logMessage("Copy operation failed");
												Notification.addMessage(Status.ERROR.toString(),
														"Iteration failed to copy. Please check the logs!");
											}
											setComponentsEnabled(true, cmbSourceProjectArea, cmbTargetProjectArea, lstTargetProjectArea,
													btnCopy, btnLoginLogout);
										} catch (InterruptedException e) {
											CustomLogger.logException(e);
										} catch (ExecutionException e) {
											CustomLogger.logException(e);
										}
									}
								};
								copyWorker.execute();
							} else {
								CustomLogger.logMessage("User clicked NO for copy operation");
								Notification.addMessage(Status.INFO.toString(), "Copy operation cancelled");
							} 
						} else {
							Notification.addMessage(Status.ERROR.toString(),
									"Please select a source project iteration and a target project iteration or a timeline to process copy!");
						}
						
					}
				});
			}
		}
	}
	
	private void setComponentsEnabled(boolean enabled, JComponent... components) {
		for (JComponent component : components) {
			component.setEnabled(enabled);
		}
	}
	
	private void buildJTree(Node node, DefaultMutableTreeNode treeNode) {
        for (Node child : node.getChildren()) {
            DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(new IterationPair(child.getIteration().getDisplay(), child.getIteration().getObj()));
            treeNode.add(childNode);
            buildJTree(child, childNode);
        }
    }

	private void refreshTargetProjectIterationsView() {
		if(cmbTargetProjectArea.getSelectedIndex() > 0){
			setComponentsEnabled(false, cmbTargetProjectArea, btnLoginLogout, btnCopy);
			ProjectAreaPair projectArea = (ProjectAreaPair) cmbTargetProjectArea.getSelectedItem();
			IterationHierarchyWorker hierarchyWorker = new IterationHierarchyWorker(projectArea.getProjectAreaObj()){
				@Override
				protected void done(){
					try {
						boolean isValid = get();
						if (isValid) {
							Notification.addMessage(Status.INFO.toString(), "Please wait! Loading the hierarchy view of Timeline");
							List<Node> iterationHierarchy = getHierarchy();
							DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(projectArea.getProjectAreaObj().getName());
							for (Node node : iterationHierarchy) {
								DefaultMutableTreeNode rootTreeNode = new DefaultMutableTreeNode(new IterationPair(node.getIteration().getDisplay(), node.getIteration().getObj()));
					            rootNode.add(rootTreeNode);
					            buildJTree(node, rootTreeNode);
							}
							targetIterationTree.setModel(new DefaultTreeModel(rootNode));
						}
						setComponentsEnabled(true, cmbTargetProjectArea, btnLoginLogout, btnCopy);
					} catch (InterruptedException e) {
						CustomLogger.logException(e);
					} catch (ExecutionException e) {
						CustomLogger.logException(e);
					}
				}
			};
			hierarchyWorker.execute();
		}
	}
	
	private void clearIterationTree(JTree tree) {
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();
		root.removeAllChildren();
		((DefaultTreeModel) tree.getModel()).reload();
		DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
		model.setRoot(null);
	}
}
