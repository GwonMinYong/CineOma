package com.ssafy.mafia.socketService;

import java.util.ArrayList;
import java.util.Iterator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

import com.ssafy.mafia.common.MafiaPlaingUser;
import com.ssafy.mafia.common.MafiaPlayStorage;
import com.ssafy.mafia.common.MafiaStaticData;
import com.ssafy.mafia.entity.Record;
import com.ssafy.mafia.entity.Room;
import com.ssafy.mafia.entity.User;
import com.ssafy.mafia.repository.RoomRepository;
import com.ssafy.mafia.repository.UserRepository;
import com.ssafy.mafia.service.RecordService;
import com.ssafy.mafia.socketDto.ProfileResultDto;
import com.ssafy.mafia.socketDto.ProfileUserDto;

import lombok.RequiredArgsConstructor;




@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService{
	
	private final SimpMessageSendingOperations sendingOperations;
	
	@Autowired
	UserRepository userRepository;
	@Autowired
	RoomRepository roomRepository;
	@Autowired
	RecordService recordService;
	
	@Override
	public void enterRoom(String id, int roomNo) throws Exception {
		User user = userRepository.findById(id);
		Room room = roomRepository.findByNo(roomNo);
		roomRepository.memberCntPlus(roomNo);
		userRepository.updateRoomNo(roomNo, id);
		
		ProfileUserDto pud = pudInUser(user);
		
		if(room.getHostId().equals(id))
			pud.setIfCaptain(true);
		else
			pud.setIfCaptain(false);
		
		
		MafiaStaticData.MafiaPlayStorageDtoMap.get(roomNo).getProfileUsers().add(pud);
		System.out.println(MafiaStaticData.MafiaPlayStorageDtoMap.get(roomNo));
	}

	@Override
	public void exitRoom(String id, int roomNo) throws Exception 
	{
		ProfileResultDto result = new ProfileResultDto();
			 
//		System.out.println("======================exitRoom========Strat====================");
		MafiaPlayStorage mps = MafiaStaticData.MafiaPlayStorageDtoMap.get(roomNo);
		
		if(mps.isRecordUpateCheck())
		{

			System.out.println("=====================????????? ???????????? ??????======================");
			System.out.println("mps");
			System.out.println(mps);
			MafiaPlaingUser mpu =mps.getMPU(id);
			String myJob=mpu.getJob();
			String winJob = mps.getWinJob();
			
			System.out.println("mpu.getId() : " + mpu.getId());
			System.out.println("mpu.getJob() : " + mpu.getJob());
			System.out.println("winJob : " + winJob);
			
			if(mpu.getId().equals("doctor") && winJob.equals("citizen"))
			{
				recordService.recordUpdate(mpu.getId(), mpu.getJob(), "true");
			}
			else if(mpu.getId().equals("citizen") && winJob.equals("doctor"))
			{
				recordService.recordUpdate(mpu.getId(), mpu.getJob(), "true");
			}
			else if(mpu.getJob().equals(winJob))
			{
				recordService.recordUpdate(mpu.getId(), mpu.getJob(), "true");
			}
			else
			{
				recordService.recordUpdate(mpu.getId(), mpu.getJob(), "lose");
			}
		}
		
		roomRepository.memberCntminus(roomNo);
		userRepository.updateRoomNo(1, id);
		int idx=0;
		ArrayList<ProfileUserDto> list = (ArrayList<ProfileUserDto>) MafiaStaticData.MafiaPlayStorageDtoMap.get(roomNo).getProfileUsers();
		for (ProfileUserDto profileUserDto : list) 
		{
			if(profileUserDto.getId().equals(id))
			{
				list.remove(idx);
				break;
			}
			idx++;
		}
		

		result.setProgress("out");
		result.setUserList(MafiaStaticData.MafiaPlayStorageDtoMap.get(roomNo).getProfileUsers());
		sendingOperations.convertAndSend("/topic/sendProfile/"+roomNo, result);
//		System.out.println("======================exitRoom ===End=========================");
	}
	
	//setIfCaptain ?????? ??? ????????????
	private ProfileUserDto pudInUser(User user) throws Exception
	{
		
		
		ProfileUserDto pud = new ProfileUserDto();
		
		pud.setId(user.getId());
		pud.setIfReady(false);
		pud.setImagePath(user.getImagePath());
		pud.setIntro(user.getIntro());
		pud.setNickName(user.getNickname());
		pud.setWinRate(makeWinRate(user.getId()));
		
		
		return pud;
	}

	private int makeWinRate(String id) throws Exception
	{
		int result =0;
		ArrayList<Record> recordList = (ArrayList<Record>) recordService.get(id);
		int denominator=0;
		int numerator=0;
		
		for (Record record : recordList) {
			denominator+=(record.getDrawCount()+record.getLoseCount()+record.getWinCount());
			numerator+=(record.getWinCount());
		}
		
		if(denominator!=0)
		{
			numerator*=100;
			result=numerator/denominator;
		}
		
		
		return result;
	}
	
}
