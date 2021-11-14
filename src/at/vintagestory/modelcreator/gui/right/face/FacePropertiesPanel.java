package at.vintagestory.modelcreator.gui.right.face;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.lang.reflect.Array;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import at.vintagestory.modelcreator.ModelCreator;
import at.vintagestory.modelcreator.Start;
import at.vintagestory.modelcreator.gui.ComponentUtil;
import at.vintagestory.modelcreator.interfaces.IElementManager;
import at.vintagestory.modelcreator.interfaces.IValueUpdater;
import at.vintagestory.modelcreator.model.Element;
import at.vintagestory.modelcreator.model.Face;
import at.vintagestory.modelcreator.util.AwtUtil;

public class FacePropertiesPanel extends JPanel implements IValueUpdater
{
	private static final long serialVersionUID = 1L;

	private IElementManager manager;

	private JPanel horizontalBox;
	private JRadioButton boxEnabled;
	private JRadioButton boxAutoUV;
	private JRadioButton boxSnapUv;
	private JTextField glowValue;
	private JTextField windData;
	
	JComboBox<String> bla = new JComboBox<String>();
	@SuppressWarnings("unchecked")
	private JComboBox<String>[] windModeList = (JComboBox<String>[]) Array.newInstance(bla.getClass(), 4);

	public FacePropertiesPanel(IElementManager manager)
	{
		this.manager = manager;
		setLayout(new BorderLayout(0, 5));
		setBorder(BorderFactory.createTitledBorder(Start.Border, "<html><b>Properties</b></html>"));
		setMaximumSize(new Dimension(250, 400));
		initComponents();
		addComponents();
	}

	public void initComponents()
	{
		horizontalBox = new JPanel(new GridLayout(0, 1));
		
		boxEnabled = ComponentUtil.createRadioButton("Enabled","<html>Determines if face should be rendered<br>Default: On</html>");
		boxEnabled.addActionListener(e ->
		{
			ModelCreator.changeHistory.beginMultichangeHistoryState();
			
			if ((e.getModifiers() & ActionEvent.SHIFT_MASK) == 1) {
				Element elem = manager.getCurrentElement();
				for (int i = 0; i < elem.getAllFaces().length; i++) {
					Face face = elem.getAllFaces()[i];
					face.setEnabled(boxEnabled.isSelected());
				}
				
			} else {
				manager.getCurrentElement().getSelectedFace().setEnabled(boxEnabled.isSelected());
			}
			
			ModelCreator.changeHistory.endMultichangeHistoryState(ModelCreator.currentProject);
		});
		
		boxAutoUV = ComponentUtil.createRadioButton("Auto Resolution", "<html>Automatically sets the UV end coordinates to fit the desired texture resolution<br>Default: On</html>");
		boxAutoUV.addActionListener(e ->
		{
			boolean on = boxAutoUV.isSelected();
			ModelCreator.changeHistory.beginMultichangeHistoryState();
			
			if ((e.getModifiers() & ActionEvent.SHIFT_MASK) == 1) {
				Element elem = manager.getCurrentElement();
				for (int i = 0; i < elem.getAllFaces().length; i++) {
					Face face = elem.getAllFaces()[i];
					face.setAutoUVEnabled(on);
					face.updateUV();
				}
				
			} else {
				manager.getCurrentElement().getSelectedFace().setAutoUVEnabled(on);
				manager.getCurrentElement().getSelectedFace().updateUV();

			}
			
			ModelCreator.updateValues(boxAutoUV);
			ModelCreator.changeHistory.endMultichangeHistoryState(ModelCreator.currentProject);
		});
		
		
		boxSnapUv = ComponentUtil.createRadioButton("Snap UV", "<html>Determines if auto-uv should snap the coordinates to pixels on the texture. Disable if your element is very small or want full control over the UV Coordinates<br>Default: On</html>");
		boxSnapUv.addActionListener(e ->
		{
			ModelCreator.changeHistory.beginMultichangeHistoryState();
			
			if ((e.getModifiers() & ActionEvent.SHIFT_MASK) == 1) {
				Element elem = manager.getCurrentElement();
				for (int i = 0; i < elem.getAllFaces().length; i++) {
					Face face = elem.getAllFaces()[i];
					face.setSnapUVEnabled(boxSnapUv.isSelected());
				}
				
			} else {
				manager.getCurrentElement().getSelectedFace().setSnapUVEnabled(boxSnapUv.isSelected());
			}
			
			
			manager.getCurrentElement().updateUV();
			ModelCreator.updateValues(boxSnapUv);
			ModelCreator.changeHistory.endMultichangeHistoryState(ModelCreator.currentProject);
		});
		
		glowValue = new JTextField();
		
		
		AwtUtil.addChangeListener(glowValue, e -> {
			try {
				manager.getCurrentElement().getSelectedFace().setGlow(Integer.parseInt(glowValue.getText()));	
			} catch(Exception ex) {
				
			}
		});
				
		horizontalBox.add(boxEnabled);
		horizontalBox.add(boxAutoUV);
		horizontalBox.add(boxSnapUv);
		horizontalBox.add(new JLabel("Glow Level (0..255)"));
		horizontalBox.add(glowValue);
		
		for (int i = 0; i < 4; i++) {
			int index=i;
			JComboBox<String> list = windModeList[i] = new JComboBox<String>(); 
			
			list.setToolTipText("Defines the wind sway behavior.");
			DefaultComboBoxModel<String> modelr = windModeList();		
			list.setModel(modelr);
			list.setPreferredSize(new Dimension(190, 25));
			
			list.addActionListener(e -> {
				Element elem = manager.getCurrentElement();
				if (elem != null) {
					Face face = elem.getSelectedFace();
					
					int prevmode = -1;
					if (face.WindModes != null) prevmode = face.WindModes[index];
					
					int newmode = list.getSelectedIndex() - 1;
					
					if (face.WindModes == null) face.WindModes = new int[] { -1, -1, -1, -1};
					face.WindModes[index]=newmode;
					
					
					if ((e.getModifiers() & ActionEvent.SHIFT_MASK) == 1) {
						if ((e.getModifiers() & ActionEvent.CTRL_MASK) == 2) {
							for (int k = 0; k < 6; k++) {
								Face f = elem.getAllFaces()[k];
								if (!f.isEnabled()) continue;
								if (f.WindModes == null) f.WindModes = new int[] { -1, -1, -1, -1};
								for (int l = 0; l < 4; l++) {
									f.WindModes[l]=newmode;
								}
							}
						}
					}
					
					
					if (prevmode != newmode) ModelCreator.DidModify();
					
					ModelCreator.updateValues(list);
				}
			});
			list.addMouseListener(new MouseListener()
			{
				
				@Override
				public void mouseReleased(MouseEvent e)
				{
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void mousePressed(MouseEvent e)
				{
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void mouseExited(MouseEvent e)
				{
					Element elem = manager.getCurrentElement();
					if (elem != null) {
						Face face = elem.getSelectedFace();
						if (face != null) {
							face.HoveredVertex = -1;
						}
					}
				}
				
				@Override
				public void mouseEntered(MouseEvent e)
				{
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void mouseClicked(MouseEvent e)
				{
					// TODO Auto-generated method stub
					
				}
			});
			list.addMouseMotionListener(new MouseMotionListener()
			{
				
				@Override
				public void mouseMoved(MouseEvent e)
				{
					Element elem = manager.getCurrentElement();
					if (elem != null) {
						Face face = elem.getSelectedFace();
						if (face != null) {
							face.HoveredVertex = index;
						}
						
					}	
					
				}
				
				@Override
				public void mouseDragged(MouseEvent e)
				{
					
					
				}
			});
			
			
			horizontalBox.add(new JLabel("Wind mode " + (i+1)));
			horizontalBox.add(list);
		}
		
		
		
		windData = new JTextField();
		horizontalBox.add(new JLabel("Wind Data"));
		horizontalBox.add(windData);
		AwtUtil.addChangeListener(windData, e -> {
			try {
				String text = windData.getText();
				String[] parts = text.split(",");
				if (parts.length == 4) {
					manager.getCurrentElement().getSelectedFace().WindData = new int[] { Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3]) };
					ModelCreator.DidModify();
				}					
			} catch(Exception ex) {
				
			}
		});
	}

	public void addComponents()
	{
		add(horizontalBox, BorderLayout.NORTH);
	}

	@Override
	public void updateValues(JComponent byGuiElem)
	{
		Element cube = manager.getCurrentElement();
		
		boxEnabled.setEnabled(cube != null);
		boxEnabled.setSelected(cube != null);
		boxAutoUV.setEnabled(cube != null);
		boxAutoUV.setSelected(cube != null);
		boxSnapUv.setEnabled(cube != null);
		glowValue.setEnabled(cube != null);
		
		windModeList[0].setEnabled(cube != null);
		windModeList[1].setEnabled(cube != null);
		windModeList[2].setEnabled(cube != null);
		windModeList[3].setEnabled(cube != null);
		
		windData.setEnabled(false);
		
		
		if (cube != null)
		{
			Face face = cube.getSelectedFace();
			
			boxEnabled.setSelected(face.isEnabled());
			boxAutoUV.setSelected(face.isAutoUVEnabled());
			boxSnapUv.setSelected(face.isSnapUvEnabled());
			if (byGuiElem != glowValue) glowValue.setText(""+face.getGlow());
			
			for (int i = 0; i < 4; i++) {
				if (face.WindModes == null) {
					windModeList[i].setSelectedIndex(0);
				} else {				
					windModeList[i].setSelectedIndex(face.WindModes[i] + 1);
				}
			}
			
			if (face.WindData != null) {
				String windDataStr = face.WindData[0]+","+face.WindData[1]+","+face.WindData[2]+","+face.WindData[3];
				if (windData.getText() != windDataStr) {
					windData.setText(windDataStr);
				}
			} else {
				windData.setText("");
			}
		}
	}
	

	private DefaultComboBoxModel<String> windModeList()
	{
		DefaultComboBoxModel<String> model = new DefaultComboBoxModel<String>();
		
		model.addElement("<html><b>Default</b></html>");
		model.addElement("<html><b>NoWind</b></html>");
		model.addElement("<html><b>WeakWind</b></html>");
		model.addElement("<html><b>NormalWind</b></html>");
		model.addElement("<html><b>Leaves</b></html>");
		model.addElement("<html><b>WeakBend</b></html>");
		model.addElement("<html><b>LeavesWeakBend</b></html>");
		model.addElement("<html><b>Other</b></html>");
		
		return model;
	}

}
