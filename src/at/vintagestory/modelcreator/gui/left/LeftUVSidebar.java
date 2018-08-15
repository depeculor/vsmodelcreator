package at.vintagestory.modelcreator.gui.left;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_LINES;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glColor3f;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glLineWidth;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glTexCoord2f;
import static org.lwjgl.opengl.GL11.glTranslatef;
import static org.lwjgl.opengl.GL11.glVertex2d;
import static org.lwjgl.opengl.GL11.glVertex2i;

import java.util.ArrayList;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.Color;
import org.newdawn.slick.opengl.TextureImpl;

import at.vintagestory.modelcreator.ModelCreator;
import at.vintagestory.modelcreator.enums.EnumFonts;
import at.vintagestory.modelcreator.interfaces.IElementManager;
import at.vintagestory.modelcreator.model.Element;
import at.vintagestory.modelcreator.model.Face;
import at.vintagestory.modelcreator.model.Sized;
import at.vintagestory.modelcreator.model.TextureEntry;

public class LeftUVSidebar extends LeftSidebar
{
	private IElementManager manager;

	private final int WIDTH = 110;

	private final Color BLACK_ALPHA = new Color(0, 0, 0, 0.75F);

	private int[] startX = { 0, 0, 0, 0, 0, 0 };
	private int[] startY = { 0, 0, 0, 0, 0, 0 };
	
	
	private int texBoxWidth, texBoxHeight;
	
	float[] brightnessByFace = new float[] { 1, 1, 1, 1, 1, 1 }; 
	
	int canvasHeight;
	
	public LeftUVSidebar(String title, IElementManager manager)
	{
		super(title);
		this.manager = manager;
	}

	@Override
	public void draw(int sidebarWidth, int canvasWidth, int canvasHeight, int frameHeight)
	{
		super.draw(sidebarWidth, canvasWidth, canvasHeight, frameHeight);

		this.canvasHeight = canvasHeight;
		
		if (ModelCreator.transparent) {
			GL11.glEnable(GL11.GL_BLEND);
			glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		}		

		if (ModelCreator.currentProject.EntityTextureMode) {
			drawRectsEntityTextureMode(canvasHeight);
		} else {
			drawRectsBlockTextureMode(canvasHeight);
		}
		
		if (ModelCreator.transparent) {
			GL11.glDisable(GL11.GL_BLEND);
		}
	}
	
	
	TextureEntry texEntry = null;
	void drawRectsEntityTextureMode(int canvasHeight) {
		double texWidth = ModelCreator.currentProject.TextureWidth;
		double texHeight = ModelCreator.currentProject.TextureHeight;
		
		Sized scale;
		
		texEntry = null;
		
		if (ModelCreator.currentProject.TexturesByCode.size() > 0) {
			texEntry = ModelCreator.currentProject.TexturesByCode.get(ModelCreator.currentProject.TexturesByCode.keySet().iterator().next());
		}
		scale = Face.getVoxel2PixelScale(texEntry);
		
		texWidth *= scale.W / 2;
		texHeight *= scale.H / 2;
		
		texBoxWidth = (int)(2 * WIDTH);
		texBoxHeight = (int)(texBoxWidth * texHeight / texWidth);
		
		glPushMatrix();
		{
			glTranslatef(10, 30, 0);

			glPushMatrix(); {
				
				glColor3f(1, 1, 1);

				if (ModelCreator.currentProject.rootElements.size() > 0) {
					Element elem = ModelCreator.currentProject.rootElements.get(0);	
					elem.getAllFaces()[0].bindTexture();
				}
				
				float endu = 1f;
				float endv = 1f;
				if (texEntry != null) {
					endu = (float)texEntry.Width / texEntry.texture.getTextureWidth();
					endv = (float)texEntry.Height / texEntry.texture.getTextureHeight();
				}

				
				glLineWidth(1F);
				glBegin(GL_QUADS);
				{
					glTexCoord2f(0, endv);
					glVertex2i(0, texBoxHeight);
					
					glTexCoord2f(endu, endv);
					glVertex2i(texBoxWidth, texBoxHeight);
					
					glTexCoord2f(endu, 0);
					glVertex2i(texBoxWidth, 0);

					glTexCoord2f(0, 0);
					glVertex2i(0, 0);
				}
				glEnd();
				
				TextureImpl.bindNone();
				
				drawElementList(ModelCreator.currentProject.rootElements, texBoxWidth, texBoxHeight, canvasHeight);
				
				
				glPopMatrix();
			}
		}
		glPopMatrix();
		
	}
	
	private void drawElementList(ArrayList<Element> elems, double texBoxWidth, double texBoxHeight, int canvasHeight)
	{
		Element selectedElem = ModelCreator.currentProject.SelectedElement;
		
		for (Element elem : elems) {
			Face[] faces = elem.getAllFaces();
			
			for (int i = 0; i < 6; i++) {
				if (!faces[i].isEnabled()) continue;
				
				Face face = faces[i];
				Sized uv = face.translateVoxelPosToUvPos(face.getStartU(), face.getStartV(), true);
				Sized uvend = face.translateVoxelPosToUvPos(face.getEndU(), face.getEndV(), true);
				
				Color color = Face.getFaceColour(i);
				
				GL11.glColor4f(color.r * elem.brightnessByFace[i], color.g * elem.brightnessByFace[i], color.b * elem.brightnessByFace[i], 0.3f);
	
				glBegin(GL_QUADS);
				{
					glTexCoord2f(0, 1);
					glVertex2d(uv.W * texBoxWidth, uvend.H * texBoxHeight);
					
					glTexCoord2f(1, 1);
					glVertex2d(uvend.W * texBoxWidth, uvend.H * texBoxHeight);
					
					glTexCoord2f(1, 0);
					glVertex2d(uvend.W * texBoxWidth, uv.H * texBoxHeight);
	
					glTexCoord2f(0, 0);
					glVertex2d(uv.W * texBoxWidth, uv.H * texBoxHeight);
				}
				glEnd();
	
				
				glColor3f(0.5f, 0.5f, 0.5f);
				if (elem == selectedElem) {
					glColor3f(0f, 0f, 1f);
				}
				if (elem == grabbedElement && face.isAutoUVEnabled()) {
					glColor3f(0f, 0.75f, 1f);
				}
				if (elem == grabbedElement && !face.isAutoUVEnabled() && i == grabbedFaceIndex) {
					glColor3f(0f, 1f, 0.75f);
				}
				

	
				glBegin(GL_LINES);
				{
					glVertex2d(uv.W * texBoxWidth, uv.H * texBoxHeight);
					glVertex2d(uv.W * texBoxWidth, uvend.H * texBoxHeight);
	
					glVertex2d(uv.W * texBoxWidth, uvend.H * texBoxHeight);
					glVertex2d(uvend.W * texBoxWidth, uvend.H * texBoxHeight);
	
					glVertex2d(uvend.W * texBoxWidth, uvend.H * texBoxHeight);
					glVertex2d(uvend.W * texBoxWidth, uv.H * texBoxHeight);
	
					glVertex2d(uvend.W * texBoxWidth, uv.H * texBoxHeight);
					glVertex2d(uv.W * texBoxWidth, uv.H * texBoxHeight);
	
				}
				glEnd();
			}
			
			drawElementList(elem.ChildElements, texBoxWidth, texBoxHeight, canvasHeight);
		}		
	}

	void drawRectsBlockTextureMode(int canvasHeight) {
		Element elem = manager.getCurrentElement();
		if (elem == null) return;
		
		float[] bright = elem != null ? elem.brightnessByFace : brightnessByFace;
		
		Sized texSize = GetBlockTextureModeTextureSize();
		
		Face[] faces = elem.getAllFaces();

		if(!Mouse.isButtonDown(0) && !Mouse.isButtonDown(1)) {
			grabbedFaceIndex = getGrabbedFace(elem, canvasHeight, Mouse.getX(), Mouse.getY());
		}
		
		
		glPushMatrix();
		{
			glTranslatef(10, 30, 0);

			
			int countleft = 0;
			int countright = 0;

			for (int i = 0; i < 6; i++) {
				
				Face face = faces[i];
				if (!face.isEnabled()) continue;
				
				glPushMatrix(); {
					if (30 + i * (WIDTH + 10) + (WIDTH + 10) > canvasHeight) {
						glTranslatef(10 + WIDTH, countright * (WIDTH + 10), 0);
						startX[i] = 20 + WIDTH;
						startY[i] = countright * (WIDTH + 10) + 40;
						countright++;
					}
					else
					{
						glTranslatef(0, countleft * (WIDTH + 10), 0);
						startX[i] = 10;
						startY[i] = countleft * (WIDTH + 10) + 40;
						countleft++;
					}

					Color color = Face.getFaceColour(i);
					glColor3f(color.r * bright[i], color.g * bright[i], color.b * bright[i]);

					face.bindTexture();

					glBegin(GL_QUADS);
					{
						glTexCoord2f(0, 1);
						glVertex2d(0, texSize.H);
						
						glTexCoord2f(1, 1);
						glVertex2d(texSize.W, texSize.H);
						
						glTexCoord2f(1, 0);
						glVertex2d(texSize.W, 0);
		
						glTexCoord2f(0, 0);
						glVertex2d(0, 0);
					}
					glEnd();
					
					glEnable(GL_BLEND);
					glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
					EnumFonts.BEBAS_NEUE_20.drawString(5, 5, Face.getFaceName(i), BLACK_ALPHA);
					glDisable(GL_BLEND);
				}
			
				glPopMatrix();
			}
			
			TextureImpl.bindNone();
			
			countleft = 0;
			countright = 0;
			
			for (int i = 0; i < 6; i++) {
				
				Face face = faces[i];
				if (!face.isEnabled()) continue;
				
				glPushMatrix(); {
					if (30 + i * (WIDTH + 10) + (WIDTH + 10) > canvasHeight) {
						glTranslatef(10 + WIDTH, countright * (WIDTH + 10), 0);
						startX[i] = 20 + WIDTH;
						startY[i] = countright * (WIDTH + 10) + 40;
						countright++;
					}
					else
					{
						glTranslatef(0, countleft * (WIDTH + 10), 0);
						startX[i] = 10;
						startY[i] = countleft * (WIDTH + 10) + 40;
						countleft++;
					}

					Sized uv = face.translateVoxelPosToUvPos(face.getStartU(), face.getStartV(), true);
					Sized uvend = face.translateVoxelPosToUvPos(face.getEndU(), face.getEndV(), true);					
					
					glColor3f(1, 1, 1);
					
					if (grabbedFaceIndex == i) {
						glColor3f(0f, 1f, 0.75f);
					}

					glBegin(GL_LINES);
					{
						glVertex2d(uv.W * texSize.W, uv.H * texSize.H);
						glVertex2d(uv.W * texSize.W, uvend.H * texSize.H);
		
						glVertex2d(uv.W * texSize.W, uvend.H * texSize.H);
						glVertex2d(uvend.W * texSize.W, uvend.H * texSize.H);
		
						glVertex2d(uvend.W * texSize.W, uvend.H * texSize.H);
						glVertex2d(uvend.W * texSize.W, uv.H * texSize.H);
		
						glVertex2d(uvend.W * texSize.W, uv.H * texSize.H);
						glVertex2d(uv.W * texSize.W, uv.H * texSize.H);
					}
					glEnd();

				}
			
				glPopMatrix();
			}
		}
		glPopMatrix();
	}
	
	

	private int lastMouseX, lastMouseY;
	private boolean grabbing = false;
	Element grabbedElement;
	int grabbedFaceIndex = -1;

	@Override
	public void mouseUp() {
		grabbing = false;
	}
	
	@Override
	public void handleInput()
	{
		super.handleInput();
		boolean nowGrabbing = Mouse.isButtonDown(0) || Mouse.isButtonDown(1);
		
		if (ModelCreator.currentProject.EntityTextureMode && !nowGrabbing) {
			grabbedElement = findElement(ModelCreator.currentProject.rootElements, Mouse.getX() - 10, (canvasHeight - Mouse.getY()) - 30);
			
		}
		if (!ModelCreator.currentProject.EntityTextureMode && !nowGrabbing) {
			grabbedElement = ModelCreator.currentProject.SelectedElement;
		}
		
		
		
		if (!grabbing && nowGrabbing) {
			if (ModelCreator.currentProject.EntityTextureMode) {
				grabbedElement = findElement(ModelCreator.currentProject.rootElements, Mouse.getX() - 10, (canvasHeight - Mouse.getY()) - 30);
			}
			else {
				grabbedElement = ModelCreator.currentProject.SelectedElement;
			}
			
			if (grabbedElement == null) return;
			
			this.lastMouseX = Mouse.getX();
			this.lastMouseY = Mouse.getY();
			
			ModelCreator.currentProject.selectElement(grabbedElement);		
			if (!ModelCreator.currentProject.EntityTextureMode) {
				grabbedFaceIndex = getGrabbedFace(grabbedElement, canvasHeight, lastMouseX, lastMouseY);	
			}
		}
		
		grabbing = nowGrabbing;
		if (grabbedElement == null) return;
		
		
		if (grabbing)
		{
			int newMouseX = Mouse.getX();
			int newMouseY = Mouse.getY();			
			int xMovement = 0;
			int yMovement = 0;
			

			if (ModelCreator.currentProject.EntityTextureMode) {
				if (texEntry != null) {
					xMovement = (int)(texEntry.VoxelWidthWithLwJglFuckery() * (newMouseX - this.lastMouseX) / texBoxWidth);
					yMovement = (int)(texEntry.VoxelHeighthWithLwJglFuckery() * (newMouseY - this.lastMouseY) / texBoxHeight);					
				} else {
					xMovement = (int)(ModelCreator.currentProject.TextureWidth * (newMouseX - this.lastMouseX) / texBoxWidth);
					yMovement = (int)(ModelCreator.currentProject.TextureHeight * (newMouseY - this.lastMouseY) / texBoxHeight);
				}

				
				if ((xMovement != 0 || yMovement != 0) && grabbedElement != null && Mouse.isButtonDown(0))
				{
					Face face = null; 
					if (grabbedFaceIndex >=0) face = grabbedElement.getAllFaces()[grabbedFaceIndex];
					if (face != null && !face.isAutoUVEnabled()) {
						
						face.moveTextureU(xMovement);
						face.moveTextureV(-yMovement);
						
					} else {
						grabbedElement.setTexUVStart(grabbedElement.getTexUStart() + xMovement, grabbedElement.getTexVStart() - yMovement);	
					}
					
					
				}
			} else {

				if (grabbedFaceIndex == -1) return;
				Face face = grabbedElement.getAllFaces()[grabbedFaceIndex];
				
				Sized texSize = GetBlockTextureModeTextureSize();
				
				TextureEntry texEntry = face.getTextureEntry();
				if (texEntry != null) {
					xMovement = (int)(texEntry.VoxelWidthWithLwJglFuckery() * (newMouseX - this.lastMouseX) / texSize.W);
					yMovement = (int)(texEntry.VoxelHeighthWithLwJglFuckery() * (newMouseY - this.lastMouseY) / texSize.H);
				} else {
					xMovement = (int)(ModelCreator.currentProject.TextureWidth * (newMouseX - this.lastMouseX) / texSize.W);
					yMovement = (int)(ModelCreator.currentProject.TextureHeight * (newMouseY - this.lastMouseY) / texSize.H);
				}

				
				if (Mouse.isButtonDown(0))
				{
					face.moveTextureU(xMovement);
					face.moveTextureV(-yMovement);
				}
				else
				{
					face.setAutoUVEnabled(false);

					face.addTextureXEnd(xMovement);
					face.addTextureYEnd(-yMovement);

					face.setAutoUVEnabled(false);
				}
				
				face.updateUV();
			}
				

			if (xMovement != 0) {
				this.lastMouseX = newMouseX;
			}
			if (yMovement != 0) {
				this.lastMouseY = newMouseY;
			}
			
			
			if (xMovement != 0 || yMovement != 0) {
				ModelCreator.updateValues(null);	
			}
			
		}
	}
	
	
	
	public Sized GetBlockTextureModeTextureSize() {
		double texWidth = ModelCreator.currentProject.TextureWidth;
		double texHeight = ModelCreator.currentProject.TextureHeight;
		int texBoxWidth = (int)(WIDTH);
		int texBoxHeight = (int)(texBoxWidth * texHeight / texWidth);

		return new Sized(texBoxWidth, texBoxHeight);
	}
	

	public int getGrabbedFace(Element elem, int canvasHeight, int mouseX, int mouseY)
	{
		if (elem == null) return -1;
		Face[] faces = elem.getAllFaces();
		if (faces == null) return -1;
		
		for (int i = 0; i < 6; i++)
		{
			if (faces[i] == null || !faces[i].isEnabled()) {
				continue;
			}
			
			if (mouseX >= startX[i] && mouseX <= startX[i] + WIDTH)
			{
				if ((canvasHeight - mouseY + 10) >= startY[i] && (canvasHeight - mouseY + 10) <= startY[i] + WIDTH)
				{
					return i;
				}
			}
		}
		
		return -1;
	}
	
	
	public int Clamp(int val, int min, int max) {
		return Math.min(Math.max(val, min), max);
	}
	
	private Element findElement(ArrayList<Element> elems, int mouseX, int mouseY)
	{
		if (texBoxHeight == 0 || texBoxWidth == 0) return null;
		
		double mouseU = (double)mouseX / texBoxWidth;
		double mouseV = (double)mouseY / texBoxHeight; 
				
		for (Element elem : elems) {
			Face[] faces = elem.getAllFaces();
			
			for (int i = 0; i < 6; i++) {
				if (!faces[i].isEnabled()) continue;
				
				Face face = faces[i];			
				Sized uv = face.translateVoxelPosToUvPos(face.getStartU(), face.getStartV(), true);
				Sized uvend = face.translateVoxelPosToUvPos(face.getEndU(), face.getEndV(), true);

				//System.out.println(mouseU + "/" + mouseV + " inside " + uv.W + "/" + uv.H +" =>" + uvend.W +"/"+uvend.H);
				
				if (mouseU >= uv.W && mouseV >= uv.H && mouseU <= uvend.W && mouseV <= uvend.H) {
					grabbedFaceIndex = i;
					return elem;
				}
			}
			
			Element foundElem = findElement(elem.ChildElements, mouseX, mouseY);
			if (foundElem != null) return foundElem;
		}
		
		return null;
	}

	
}
