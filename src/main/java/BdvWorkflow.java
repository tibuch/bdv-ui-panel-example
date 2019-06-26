import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.Future;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import org.scijava.command.Command;
import org.scijava.module.MethodCallException;
import org.scijava.module.Module;
import org.scijava.module.ModuleException;
import org.scijava.module.ModuleInfo;
import org.scijava.ui.swing.widget.SwingInputHarvester;
import org.scijava.ui.swing.widget.SwingInputPanel;
import org.scijava.widget.InputPanel;

import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import bdv.util.BdvStackSource;
import bdv.util.BdvUIPanel;
import net.imagej.ImageJ;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.miginfocom.swing.MigLayout;

public class BdvWorkflow<T extends RealType<T>> {

	private final ImageJ ij;
	private final BdvUIPanel bdv;
	private BdvStackSource<? extends RealType<?>> dog_pyramid_source = null;
	private final SwingInputHarvester sih;

	public BdvWorkflow(final ImageJ ij, final BdvUIPanel bdv) {
		this.ij = ij;
		this.bdv = bdv;
		sih = new SwingInputHarvester();
		sih.setContext(ij.context());
	}

	public void addDoGPyramid(final RandomAccessibleInterval<T> img) {
		final Module module = createModule(ij, DoG.class);

		module.setInput("log", ij.log());
		module.resolveInput("log");
		module.setInput("ops", ij.op());
		module.resolveInput("ops");
		module.setInput("image", img);
		module.resolveInput("image");

		final JPanel inputPanel = buildInputPanel(bdv, ij, module, "DoG Pyramid", "output");

		bdv.addNewCard("Blob Detection", true, inputPanel);
	}

	private JPanel buildInputPanel(final BdvUIPanel bdv, final ImageJ ij, final Module module, final String panelTitle,
			final String outputName) {

		final InputPanel<JPanel, JPanel> inputPanel = new SwingInputPanel();
		try {
			sih.buildPanel(inputPanel, module);
		} catch (ModuleException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		final JPanel p = new JPanel(new MigLayout());
		p.setBorder(new TitledBorder(new LineBorder(Color.lightGray), panelTitle));
		p.setBackground(Color.white);
		p.add(inputPanel.getComponent(), "wrap");

		final JButton runButton = new JButton("Run");
		runButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				runButton.setEnabled(false);
				Future<Module> run = ij.module().run(module, true, module.getInputs());
				ij.thread().getExecutorService().submit(new Runnable() {

					@Override
					public void run() {
						if (dog_pyramid_source != null) {
							dog_pyramid_source.removeFromBdv();
							dog_pyramid_source = null;
						}
						Module m = ij.module().waitFor(run);
						RandomAccessibleInterval<T> output = (RandomAccessibleInterval<T>) m.getOutput(outputName);
						dog_pyramid_source = BdvFunctions.show(output, "DoG Pyramid", BdvOptions.options().addTo(bdv));
						if (bdv.getBdvHandle().getViewerPanel().getVisibilityAndGrouping().isFusedEnabled()) {
							bdv.getBdvHandle().getViewerPanel().getVisibilityAndGrouping().setFusedEnabled(false);
						}
						dog_pyramid_source.setCurrent();
						runButton.setEnabled(true);
					}
				});
			}
		});

		p.add(runButton);

		return p;
	}

	private <C extends Command> Module createModule(final ImageJ ij, Class<C> clazz) {
		final ModuleInfo info = ij.command().getCommand(clazz);

		final Module module = ij.module().createModule(info);
		try {
			module.initialize();
		} catch (MethodCallException e1) {
			e1.printStackTrace();
		}
		return module;
	}

}
