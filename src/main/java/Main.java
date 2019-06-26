import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;

import javax.swing.JFrame;
import javax.swing.JPanel;

import bdv.util.Bdv;
import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import bdv.util.BdvOverlay;
import bdv.util.BdvUIPanel;
import net.imagej.ImageJ;
import net.imglib2.img.Img;
import net.imglib2.realtransform.AffineTransform2D;
import net.imglib2.type.numeric.RealType;
import net.miginfocom.swing.MigLayout;
import sc.fiji.simplifiedio.SimplifiedIO;

public class Main {

	public static <T extends RealType<T>> void main(String[] args) {
		// Launch imagej and load an image
		final ImageJ ij = new ImageJ();
		final Img<T> image = SimplifiedIO.openImage("https://imagej.net/images/blobs.gif").getImg();

		// Create a bdv-ui-panel
		final BdvUIPanel bdv = createBdvUiFrame();

		// Use UI components individually
//		final BdvUIPanel bdv = new BdvUIPanel(null, BdvOptions.options().preferredSize(1000, 800).is2D());
//		final JPanel controlPanel = createIndividualFrames(bdv);

		// Create a bdv workflow with the DoG-Pyramid command
		final BdvWorkflow<T> workflow = new BdvWorkflow<>(ij, bdv);
		workflow.addDoGPyramid(image);

		// Add new Blob Detection panel to the control panel
//		controlPanel.add(bdv.getCard("Blob Detection").getComponent(1), "growx");

		// Display the loaded image
		BdvFunctions.show(image, "Blobs", BdvOptions.options().addTo(bdv));

		final BdvOverlay overlay = new BdvOverlay() {
			@Override
			protected void draw(final Graphics2D g) {
				g.setColor(Color.RED);
				final double[] p0 = { 50, 50 };
				final double[] p1 = { 350, 350 };
				g.drawLine((int) p0[0], (int) p0[1], (int) p1[0], (int) p1[1]);

				/*
				 * The BdvOverlay super class provides methods to get the current
				 * transformation.
				 */
				AffineTransform2D transform = new AffineTransform2D();
				this.getCurrentTransform2D( transform );

				final double[] p0t = new double[ 2 ];
				final double[] p1t = new double[ 2 ];
				transform.apply( p0, p0t );
				transform.apply( p1, p1t );
				g.setColor( new Color( info.getColor().get() ) );
				g.drawLine(
						( int ) p0t[ 0 ],
						( int ) p0t[ 1 ],
						( int ) p1t[ 0 ],
						( int ) p1t[ 1 ] );

				/*
				 * Overlays are added as fake sources, so their color, brightness, visibility
				 * can be modified through the UI. The BdvOverlay super class has a field "info"
				 * that provides access to those properties.
				 */
			}
		};

		BdvFunctions.showOverlay(overlay, "overlay", Bdv.options().addTo(bdv));

		// Close interpolation card
		bdv.toggleCard("Interpolation");
	}

	private static JPanel createIndividualFrames(final BdvUIPanel bdv) {
		final JFrame viewerFrame = new JFrame("Viewer");
		viewerFrame.setBounds(50, 50, 1200, 900);
		viewerFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		viewerFrame.setVisible(true);

		viewerFrame.add(bdv.getViewerPanel());
		viewerFrame.pack();

		final JFrame controlFrame = new JFrame("Controls");
		controlFrame.setBounds(1100, 50, 320, 800);
		controlFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		controlFrame.setVisible(true);

		final JPanel controlPanel = new JPanel(new MigLayout("fillx", "[grow]", ""));
		controlPanel.setBackground(Color.white);
		controlPanel.setPreferredSize(new Dimension(320, 800));
		controlPanel.add(bdv.getSelectionAndGroupingPanel(), "growx, wrap");
		controlPanel.add(bdv.getTransformationPanel(), "growx, wrap");
		controlPanel.add(bdv.getInterpolationPanel(), "growx, wrap");

		controlFrame.add(controlPanel);
		controlFrame.pack();
		return controlPanel;
	}

	private static BdvUIPanel createBdvUiFrame() {
		final JFrame frame = new JFrame("BDV-UI");
		frame.setBounds(50, 50, 1200, 900);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setVisible(true);
		final BdvUIPanel bdv = new BdvUIPanel(frame, BdvOptions.options().preferredSize(1200, 800).is2D());
		frame.pack();
		return bdv;
	}

}
