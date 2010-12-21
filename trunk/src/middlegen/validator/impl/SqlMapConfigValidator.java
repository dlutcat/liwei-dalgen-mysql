package middlegen.validator.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import middlegen.FileProducer;
import middlegen.Util;
import middlegen.validator.ErrorMessage;
import middlegen.validator.ValidatorAdapter;

import org.apache.commons.digester.Digester;

import com.alibaba.common.lang.StringUtil;
/**
 * sqlmap����У����
 * ����֤��������ɵ��ļ��ͼ������滻��ԭ�ļ����Ƚϣ�
 * ���ԭ�ļ��ж����resource�������������ļ�����������֤ʧ��
 * @author yong.liuhy
 *
 */
public class SqlMapConfigValidator extends ValidatorAdapter {


		public List<ErrorMessage> validateAfterGenerate(File generatedFile, File replacedFile, FileProducer fileProcucer) {
			
			List<ErrorMessage> result=new ArrayList<ErrorMessage>();
			
			List<SqlMapResource> generatedResources=new SqlMapResourcesHandler().parse(generatedFile);
			List<SqlMapResource>  replacedResources=new SqlMapResourcesHandler().parse(replacedFile);
			
			String temFileName=StringUtil.substringAfterLast(fileProcucer.getTemplate().getFile(),"/");
			String replacedFileName=fileProcucer.getDestinationFileName();
			
			if(replacedResources!=null && replacedResources.size()>0)
			{
				if(generatedResources==null || generatedResources.size()<=0)
				{
					ErrorMessage message=new ErrorMessage();
					message.setMessage("ģ���ļ�:"+temFileName+"���ɵ�sqlmap����Ϊ0����ԭ�ļ�:"+replacedFileName+"��sqlmap����Ϊ"+replacedResources.size()+"��������ģ���ļ������Ƿ���ȷ��");
					result.add(message);
					return result;
				}
				
				for(SqlMapResource resource : replacedResources)
				{
					if(!generatedResources.contains(resource))
					{
						ErrorMessage message=new ErrorMessage();
						message.setMessage(resource+"�ڣ�"+replacedFileName+"���ж���,����ģ���ļ���"+temFileName+"�ļ���ȴû�ж��壬�������������ģ���ļ��ж����resource ��");
						result.add(message);
					}
				}
			}
			
			return result;
		}



	   private static class SqlMapResourcesHandler {
		   
	      private final List<SqlMapResource> resources;
	      
	      public void addResource(String str)
	      {
	    	  this.resources.add(new SqlMapResource(str));
	      }

		  public List<SqlMapResource> getResources() {
				return resources;
		  }
			  
	      public SqlMapResourcesHandler() {
	         this.resources=new ArrayList<SqlMapResource>();
	      }
		      
		  public List<SqlMapResource> parse(File sqlMapFile) {
		      try {
			     Digester digester = new Digester();
	             digester.setValidating(false);
	             digester.push(this);
	             digester.addCallMethod("sqlMapConfig/sqlMap", "addResource", 1, new String[]{"java.lang.String"});
	             digester.addCallParam("sqlMapConfig/sqlMap", 0, "resource");
	             
	             String str=Util.trimDocType(sqlMapFile);
	             
	             digester.parse(new StringReader(str));
	             
	             return this.getResources();
	             
		      } catch (Exception e) {
		         e.printStackTrace();
		         throw new IllegalStateException(sqlMapFile.getAbsolutePath()+  "�������������ʽ�Ƿ���ȷ" );
		      }
		   }
	  }
	   
	   
	  public static class SqlMapResource
	  {
		  
		    public SqlMapResource(String resource)
			{
				  this.resource=resource;
			}
			  
		  	private String resource;
	
			public String getResource() {
				return resource;
			}
	
			public void setResource(String resource) {
				this.resource = resource;
			}
			
			public String toString()
			{
				return "[resource="+this.resource+"]";
			}
			
			public boolean equals(Object obj)
			{
				return StringUtil.equalsIgnoreCase(((SqlMapResource)obj).resource ,resource);
			}
	  }
	  
	  
	   
	  public static void main(String args[])throws Exception
	  {
		  FileInputStream in=new FileInputStream("D:\\test.xml");
		  byte[] b=new byte[in.available()];
		  in.read(b);
		  in.close();
		  
		  File file=File.createTempFile("ccc", null);
		  FileOutputStream out=new FileOutputStream(file);
		  out.write(b);
		  out.flush();
		  out.close();
		  
		  URL url=new URL("file:D:/projects/yong_liuhy_CP-mipgw-unittest-080-20091223_intg/vobs/mipgw/mipgw-dalgen/templates/beans-dal-apayfund-dao.vm");
		  
		  System.out.println(StringUtil.substringAfterLast(url.getFile(),"/"));
	  }
}

