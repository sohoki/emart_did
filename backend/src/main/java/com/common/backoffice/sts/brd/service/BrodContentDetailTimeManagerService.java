package com.common.backoffice.sts.brd.service;




import com.common.backoffice.sts.brd.mapper.BrodContentDetailTimeManagerMapper;
import com.common.backoffice.sts.brd.modals.BrodContentDetailTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
@Slf4j
public class BrodContentDetailTimeManagerService  {

	
	private final BrodContentDetailTimeManagerMapper brodTime;


    @Transactional(readOnly = false)
	public int insertBrodContentDetailTime(BrodContentDetailTime vo)
			throws Exception {
		// TODO Auto-generated method stub
		return brodTime.insertBrodContentDetailTime(vo);
	}

    @Transactional(readOnly = false)
	public int deleteBrodContentDetailTime(String imsiSeq) throws Exception {
		// TODO Auto-generated method stub
		return brodTime.deleteBrodContentDetailTime(imsiSeq);
	}

    @Transactional(readOnly = false)
	public int deleteBrodContentDetailTimeBrodCode(String brodCode)
			throws Exception {
		// TODO Auto-generated method stub
		return brodTime.deleteBrodContentDetailTimeBrodCode(brodCode);
	}

}
