package middlegen.validator;

import java.io.File;
import java.util.List;

import middlegen.FileProducer;

/**
 * validatorÄ¬ÈÏÊÊÅäÆ÷
 * 
 * @author yong.liuhy
 *
 */
public abstract class ValidatorAdapter implements Validator {

	public List<ErrorMessage> validateAfterGenerate(File generatedFile,
			File replacedFile, FileProducer fileProcucer) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<ErrorMessage> validateBeforeGenerate(FileProducer fileProcucer) {
		// TODO Auto-generated method stub
		return null;
	}

}
