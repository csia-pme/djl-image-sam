package djlsam;

import ai.djl.MalformedModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.transform.Normalize;
import ai.djl.modality.cv.transform.Resize;
import ai.djl.modality.cv.transform.ToTensor;
import ai.djl.modality.cv.translator.SemanticSegmentationTranslatorFactory;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.TranslateException;
import ai.djl.translate.Translator;
import djlsam.translators.SamRawOutput;
import djlsam.translators.SamTranslator;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * Class for the SAM model wrapping the TorchScript SAM model.
 */
public class Sam {
	private final Predictor<Image, SamRawOutput> predictor;

	public Sam() {
		Translator<Image, SamRawOutput> translator = SamTranslator.builder()
				.addTransform(new Resize(1024))
				.addTransform(new ToTensor())
				// Normalize with mean and std of ImageNet / 255
				.addTransform(new Normalize(new float[]{0.485f, 0.456f, 0.406f}, new float[]{0.229f, 0.224f, 0.225f}))
				.build();
		Criteria<Image, SamRawOutput> criteria = Criteria.builder()
				.setTypes(Image.class, SamRawOutput.class)
				.optModelPath(Paths.get("src/resources/pytorch_models/sam_vit_b"))
				.optTranslatorFactory(new SemanticSegmentationTranslatorFactory())
				.optEngine("PyTorch")
				.optTranslator(translator)
				.optProgress(new ProgressBar())
				.build();
		ZooModel<Image, SamRawOutput> model;
		try {
			model = criteria.loadModel();
		} catch (IOException | ModelNotFoundException | MalformedModelException e) {
			throw new RuntimeException(e);
		}
		this.predictor = model.newPredictor();
	}

	/**
	 * @param image Image to predict on
	 * @return SamRawOutput of the output from the model
	 */
	public SamRawOutput predict(Image image) {
		try {
			return predictor.predict(image);
		} catch (TranslateException e) {
			throw new RuntimeException(e);
		}
	}
}
