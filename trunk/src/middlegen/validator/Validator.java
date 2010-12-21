package middlegen.validator;

import java.io.File;
import java.util.List;

import middlegen.FileProducer;

/**
 * ��֤���ӿ�
 * @author yong.liuhy
 *
 */
public interface Validator
{
	/**
	 * ���ɺ���֤��������صĴ����б�Ϊ�գ�����ֹ����dalgen���ɶ���
	 * @param generatedFile ��Ҫ���ɵ��ļ�
	 * @param replacedFile �����滻��ԭ�ļ�
	 * @param fileProcucer 
	 * @return 
	 */
	public List<ErrorMessage> validateAfterGenerate(File generatedFile, File replacedFile, FileProducer fileProcucer);
	
	
	/**
	 * ����ǰ��֤�� ������صĴ�����Ϣ�б�Ϊ�գ�����ֹ����dalgen����
	 * @param fileProcucer
	 * @return
	 */
	public List<ErrorMessage> validateBeforeGenerate(FileProducer fileProcucer);
	
}