package com.tower.ggk.wsserver; 

import org.apache.axiom.om.OMElement;

public interface IGGKService{ 

	//�Զ���Webservice�����ڼ��� �����ʽ��Ρ� �ӿڷ���   �������MT_CPMIS2OA_Req  �ز�����MT_CPMIS2OA_Resp
	public OMElement MT_CPMIS2OA_Req(OMElement soapBody);
	//վַ�������սӿ�
	public String GG_INTERFACE_Syn(String info);
}