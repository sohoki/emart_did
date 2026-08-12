/*
 * detailMake1 ~ 3의 MODE : S(Single) / T(Twin)
 */

function swiper_setting(){
	var swiper = new Swiper('.swiper-container', {
	  spaceBetween: 0,
	  centeredSlides: true,
	  loop: false,
	  autoplay: {
		delay: 20000,
		disableOnInteraction: false,
	  },
	  navigation: {
		nextEl: '.swiper-button-next',
		prevEl: '.swiper-button-prev'
	  }
	});
}

function clock(){
	var dt = new Date();
	var day = "";
	switch(dt.getDay()){
		case 0 : day = "일요일"; break;
		case 1 : day = "월요일"; break;
		case 2 : day = "화요일"; break;
		case 3 : day = "수요일"; break;
		case 4 : day = "목요일"; break;
		case 5 : day = "금요일"; break;
		case 6 : day = "토요일"; break;
	}
	$(".fix_view_monthday").text((dt.getMonth()+1) + "월 "+dt.getDate() + "일");
	$(".fix_view_dayOfWeek").text(day);
	$(".fix_view_dateTime").text(dt.getHours() + ":" + zeroAdd(dt.getMinutes()));
	setTimeout("clock()", 1000);
}

function zeroAdd(data){
	if(data.toString().length == 1){
		data = "0"+data;
	} else {
		data = data.toString();
	}
	return data;
}

function timeView(data){
	return data.substring(0,2) +":"+ data.substring(2,4);
}

// 시간테이블 생성 함수
function timeTable(){
	var dt = new Date();
	
	// header & footer
	var times = new Array();
	var tableCnt = (21-dt.getHours())*2;
	var minuteWriteZero = true;
	
	if(dt.getMinutes() >= 30){
		tableCnt--;
		minuteWriteZero = false;
	} else {
		minuteWriteZero = true;
	}
	
	var timeTableData = new Array();
	var pushData = "";
	var minuteTxt = "00";
	var hourChk = 0;

	hourChk = dt.getHours();

	for(var i = 0; i < tableCnt; i++){
		if(minuteWriteZero){
			if(i > 0){
				hourChk++;
			}
			minuteTxt = "00";
		} else {
			minuteTxt = "30";
		}
		pushData = {table_hour:zeroAdd(hourChk), table_minute:minuteTxt}; 
		timeTableData.push(pushData);
		minuteWriteZero = !minuteWriteZero;
	}
	// console.log(timeTableData);
}

function pageMake(mode, data, data2){
	var headerSource = "";
	var contentsSource = "";
	var footerSource = "";

	
	var xml;
	var jsonData;
	var realData;
	var xml2;
	var jsonData2;
	var realData2;
	
	if(data == "NETWORK_ERROR"){
		realData == "NETWORK_ERROR";
	} else {
		if(data == "ERROR"){
			realData = "";
		} else {
			xml = $.parseXML(data);
			jsonData = JSON.parse($(xml).find("string").text());
			realData = jsonData.RoomDetail;	
		}

		if(mode == "T"){
			if(data2 == "ERROR"){
				realData2 = "";
			} else {
				xml2 = $.parseXML(data2);
				jsonData2 = JSON.parse($(xml2).find("string").text());
				realData2 = jsonData2.RoomDetail;	
			}
		}
	}
	

	// [TYPE : 고정생성] [Header 영역]
	createHeader(mode, realData, realData2); 

	/* ##### START [TYPE : 고정] [기본 콘텐츠 영역] */
	// 여기서 데이터 호출
	if(mode == "T") $(".main_slider").addClass("two_container");
	contentsSource += "<div class='swiper-container'><div class='swiper-wrapper'>";
	contentsSource += detailMake1(mode, realData, realData2); // 상세페이지 01 - 메인 
	contentsSource += detailMake2(mode, realData, realData2); // 상세페이지 02 - 다음예약 
	// contentsSource += detailMake3(mode, jsonData.RoomDetail, jsonData2.RoomDetail); // 상세페이지 03 - 전체예약
	contentsSource += "<div class='swiper-pagination'></div></div></div>";  
	$(".main_slider").html(contentsSource);
	/* [기본 콘텐츠 영역] FINISH ##### */
	
	// [TYPE : 고정생성] [Header 영역]
	createFooter(mode); 
	
	//htmlSource += "";
	//htmlSource += "";
	swiper_setting();
}

// 페이지기본 - Header
function createHeader(mode, data, data2){
	var headerSource = "";
	var dataRoomname = "";
	var data2Roomname = "";
	
	if(data != "NETWORK_ERROR"){
		if(data != "ERROR"){
			dataRoomname = data.roomname;
		}
		
		if(data2 != "ERROR"){
			if(mode == "T"){
				data2Roomname = data2.roomname;
			}
		}
		
	}

	if(mode == "S"){
		headerSource += "<div class='float_left single_room'><span class='room_name' title='회의실명'>"+dataRoomname+"</span><span class='floor' title='회의실 층수 및 기본정보'></span></div>"; // meeting room basic info
		headerSource += "<div class='float_right'><ul class='date'><li class='fix_view_monthday'></li><li class='fix_view_dayOfWeek'></li></ul><p class='time fix_view_dateTime'></p></div>"; // date & time
		headerSource += "<div class='clear'></div>";
	} else {
		headerSource += "<div class='two_box left_room'><span class='room_name' title='회의실명'>"+dataRoomname+"</span><span class='floor' title='회의실 층수 및 기본정보'></span></div>"; // twin - left room info
		headerSource += "<div class='two_box right_room'><div class='padding-left45'><span class='room_name' title='회의실명'>"+data2Roomname+"</span><span class='floor' title='회의실 층수 및 기본정보'></span></div></div>"; // twin - right room info
		headerSource += "<div class='clear'></div>";
	}
	
	
	
	$(".header .contents").html(headerSource);
	if(mode == "S"){
		clock();
	}
}

function createFooter(mode){
	var headerSource = "";
	if(mode == "T"){
		headerSource += "<h1 class='float_left'><img src='img/logo.png' alt='이마트로고'></h1>";
		headerSource += "<div class='float_right two_footer' style='color:#ffffff;'>";
		headerSource += "<ul class='date'><li class='fix_view_monthday'></li><li class='fix_view_dayOfWeek'></li></ul>";
		headerSource += "<p class='time fix_view_dateTime'></p></div>";
		headerSource += "<div class='clear'></div>";    
		$(".footer .contents").html(headerSource);
		clock();
	}
}

// 상세페이지 01 - 메인
function detailMake1(mode, data, data2){
	var htmlSource = "";
	htmlSource += "<div class='swiper-slide slide01'><div class='contents'>";
	
	if(data == "NETWORK_ERROR"){
		htmlSource = netError();
	} else {
		if(mode == "S"){
			if( data == "ERROR"){
				htmlSource += dataError();
			} else {
				if(data.resvstatus == "1"){ // 현재시간을 기준으로 하여 진행중 / 다음 예약인지 확인하여 표출 할 필요가 있음 -> 그에 따라 if문 조건 변경
					htmlSource += "<div class='margin-top90'>";
					htmlSource += "<div class='meeting_tit'><p class='meeting_noti'>회의제목</p><h3 class='meeting_txt'>"+data.resvtitle+"</h3></div>";
					htmlSource += "<div class='float_right'><span class='onMeeting'>"+timeView(data.resvusetimefrom)+" ~ "+timeView(data.resvusetimeto)+" 진행중</span></div>";
					htmlSource += "<div class='clear'></div>";
					htmlSource += "</div>";
					htmlSource += "<div class='user_nam text-right'><p class='meeting_noti'>사용자</p><p class='meeting_part'>"+data.resvusrdeptname+"</p><p class='meeting_user'>"+data.resvusrname+" "+data.resvposname+"</p></div>";
				} else {
					htmlSource += noneReservation(mode, 1, data);
				}
				htmlSource += "<div class='next_reser'><ul><li>다음 예약</li><li>";
				if(data.resvusetimefromnext != ""){
					htmlSource += timeView(data.resvusetimefromnext)+" ~ "+timeView(data.resvusetimetonext)+" "+data.resvusrnamenext+" "+data.resvposnamenext;
				} else {
					htmlSource += "예정 된 회의가 없습니다.";	
				}
				htmlSource += "</li></ul><div class='clear'></div></div>";
			} 
		} else if(mode == "T"){
			var dt = new Date();
			var time = zeroAdd(dt.getHours()) +":"+ zeroAdd(dt.getMinutes());
			
			// 좌측 회의실 ----------------------------------------------------------------------------------------------------------------------
			htmlSource += "<div class='two_box'><div class='padding-right45'>";
			if( data == "ERROR"){
				htmlSource += dataError();
			} else {
				if(data.resvstatus == "1"){
					htmlSource += "<div class='margin-top80'><span class='two_onMeeting'>"+timeView(data.resvusetimefrom)+" ~ "+timeView(data.resvusetimeto)+" 진행중</span></div>";
					htmlSource += "<h3 class='two_tit'>"+data.resvtitle+"</h3>";      
					htmlSource += "<div class='two_noti'><p class='meeting_part'>"+data.resvusrdeptname+"</p><p class='meeting_user'>"+data.resvusrname+" "+data.resvposname+"</p></div>";
				} else {
					// 좌측 - 현재 회의없음
					var meetOff = "";
					if(data.resvusetimefromnext == ""){
						// 좌측 - 현재 회의없음 && 다음 예약 없음
						meetOff = time + " ~";
					} else {
						// 좌측 - 현재 회의없음 && 다음 예약 있음
						meetOff = time + " ~ " + timeView(data.resvusetimefromnext);
					}
					htmlSource += "<div class='none_reser'><p class='none_tit'>진행 중인 회의가 없습니다.</p><span class='two_offMeeting'> " + meetOff + " 회의없음</span></div>";
				}
				htmlSource += "<div class='next_reser'><ul><li>다음예약</li><li>";
				if(data.resvusetimefromnext != ""){
					htmlSource += timeView(data.resvusetimefromnext)+" ~ "+timeView(data.resvusetimetonext)+" "+data.resvusrnamenext+" "+data.resvposnamenext;	
				} else {
					// 다음 예약이 없음
					htmlSource += "예정 된 회의가 없습니다.";
				}
				htmlSource += "</li></ul><div class='clear'></div></div>"; // 다음예약과 엮이는 소스
			} 
			
			htmlSource += "</div></div>"; 

			// 우측 회의실 ----------------------------------------------------------------------------------------------------------------------
			htmlSource += "<div class='two_box'><div class='padding-left45 two_border'>";
			if( data2 == "ERROR"){
				htmlSource += dataError();
			} else {
				if(data2.resvstatus == "1"){
					htmlSource += "<div class='margin-top80'><span class='two_onMeeting'>"+timeView(data2.resvusetimefrom)+" ~ "+timeView(data2.resvusetimeto)+" 진행중</span></div>";
					htmlSource += "<h3 class='two_tit'>"+data2.resvtitle+"</h3>";      
					htmlSource += "<div class='two_noti'><p class='meeting_part'>"+data2.resvusrdeptname+"</p><p class='meeting_user'>"+data2.resvusrname+" "+data2.resvposname+"</p></div>";
				} else {
					// 우측 - 현재 회의없음
					var meetOff = "";
					if(data2.resvusetimefromnext == ""){
						// 우측 - 현재 회의없음 && 다음 예약 없음
						meetOff = time + " ~";
					} else {
						// 우측 - 현재 회의없음 && 다음 예약 있음
						meetOff = time + " ~ " + timeView(data2.resvusetimefromnext);
					}
					htmlSource += "<div class='none_reser'><p class='none_tit'>진행 중인 회의가 없습니다.</p><span class='two_offMeeting'> " + meetOff + " 회의없음</span></div>";
				}
				htmlSource += "<div class='next_reser'><ul><li>다음예약</li><li>";
				if(data2.resvusetimefromnext != ""){
					htmlSource += timeView(data2.resvusetimefromnext)+" ~ "+timeView(data2.resvusetimetonext)+" "+data2.resvusrnamenext+" "+data2.resvposnamenext;	
				} else {
					// 다음 예약이 없음
					htmlSource += "예정 된 회의가 없습니다.";
				}
				htmlSource += "</li></ul><div class='clear'></div></div>";
			} 
			htmlSource += "</div></div>";
		}
	}
	htmlSource += "</div></div>";
	return htmlSource;
}

// 상세페이지 02 - 다음예약
function detailMake2(mode, data, data2){
	var htmlSource = "";
	htmlSource += "<div class='swiper-slide slide02'><div class='contents'>";
	
	if(data == "NETWORK_ERROR"){
		htmlSource = netError();
	} else {
		if(mode == "S"){
			if( data == "ERROR"){
				htmlSource += dataError();
			} else if (data.resvusetimefromnext != ""){
				htmlSource += "<div class='margin-top90'>";
				htmlSource += "<div class='meeting_tit'><p class='meeting_noti'>회의제목</p><h3 class='meeting_txt'>"+data.resvtitlenext+"</h3></div>";
				htmlSource += "<div class='float_right'><span class='offMeeting'>"+timeView(data.resvusetimefromnext)+" ~ "+timeView(data.resvusetimetonext)+" 진행예정</span></div>";
				htmlSource += "<div class='clear'></div></div>";
				htmlSource += "<div class='user_nam text-right'><p class='meeting_noti'>사용자</p><p class='meeting_part'>"+data.resvusrdeptnamenext+"</p><p class='meeting_user'>"+data.resvusrnamenext+" "+data.resvposnamenext+"</p></div>";
			} else {
				htmlSource += noneReservation(mode, 2, data);
			}
		} else if(mode == "T"){
			var dt = new Date();
			var time = zeroAdd(dt.getHours()) +":"+ zeroAdd(dt.getMinutes());
			
			// 좌측 회의실 ----------------------------------------------------------------------------------------------------------------------
			htmlSource += "<div class='two_box'><div class='padding-right45'>";
			if( data == "ERROR"){
				htmlSource += dataError();
			} else if (data.resvusetimefromnext != ""){

				htmlSource += "<div class='margin-top80'><span class='two_offMeeting'>"+timeView(data.resvusetimefromnext)+" ~ "+timeView(data.resvusetimetonext)+" 진행예정</span></div>";        
				htmlSource += "<h3 class='two_tit'>"+data.resvtitlenext+"</h3><div class='two_noti'><p class='meeting_part'>"+data.resvusrdeptnamenext+"</p><p class='meeting_user'>"+data.resvusrnamenext+" "+data.resvposnamenext+"</p></div>";                     	
			} else {
				htmlSource += noneReservation(mode, 2, data);
			}
			htmlSource += "</div></div>";
			
			// 우측 회의실 ----------------------------------------------------------------------------------------------------------------------
			htmlSource += "<div class='two_box'><div class='padding-left45 two_border'>";
			if( data2 == "ERROR"){
				htmlSource += dataError();
			} else if(data2.resvusetimefromnext != ""){
				htmlSource += "<div class='margin-top80'><span class='two_offMeeting'>"+timeView(data2.resvusetimefromnext)+" ~ "+timeView(data2.resvusetimetonext)+" 진행예정</span></div>";        
				htmlSource += "<h3 class='two_tit'>"+data2.resvtitlenext+"</h3><div class='two_noti'><p class='meeting_part'>"+data2.resvusrdeptnamenext+"</p><p class='meeting_user'>"+data2.resvusrnamenext+" "+data2.resvposnamenext+"</p></div>";                     
			} else {
				htmlSource += noneReservation(mode, 2, data2);
			}
			htmlSource += "</div></div>";
		}									
	}
	htmlSource += "</div></div>";
	return htmlSource;
}

// 상세페이지 03 - 전체예약
function detailMake3(mode, data, data2){
	var htmlSource = "";
	htmlSource += "<div class='swiper-slide slide03'><div class='contents'>";
	if(mode == "S"){
		htmlSource += "<h2 class='slider_tit'>회의실 예약 현황</h2>";
		htmlSource += "<table class='meeting_list'>";
		htmlSource += "<thead><tr><th>시간</th><th>상태</th><th>회의주제</th><th>부서</th><th>사용자</th></tr></thead>";
		htmlSource += "<tbody>";
		htmlSource += "<!-- 현재시간 라인의 tr에 추가할 class 'meeting_active' -->";
		htmlSource += "<!-- 예약시간 정보 내 span 태그의 설정 class | 진행중인 시간 : 'meeting_on' / 예약이 되어있는 시간 : 'meeting_off' / 예약이 없는 시간 : 'meeting_none' -->";
		htmlSource += "<!-- 1 : 시간정보 / 2 : 회의 진행중, 회의 없음, 회의 진행 예정 / 3 : 회의 주제 / 4 : 예약자 부서 / 5 : 예약자명 -->";
		for(var i = 0; i < data.length; i++){
			htmlSource += "<tr><td>"+data[i].reserv_time_from+" ~ "+data[i].reserv_time_to+"</td>";
			if(i == 0){
				htmlSource += "<td><span class='meeting_on'>진행중</span></td>";
			} else { // if(i == 1){
				htmlSource += "<td><span class='meeting_off'>진행 예정</span></td>";
			}/* else {
				htmlSource += "<td><span class='meeting_none'>회의 없음</span></td>";
			}	*/
			htmlSource += "<td>"+data[i].reserv_nm+"</td>";
			htmlSource += "<td>"+data[i].reserv_emp_info+"</td>";
			htmlSource += "<td>"+data[i].reserv_emp_info+"</td></tr>"; // 반복문 돌릴 구역
		}
		htmlSource += "</tbody>";
		htmlSource += "</table>";
	} else if(mode == "T"){
		
	}
	htmlSource += "</div></div>";
	return htmlSource;
}

// 현재 및 다음 예약 정보가 없는 경우 
function noneReservation(mode, type, data){
	// type : page정보이며, 메인페이지의 경우 다음 예약정보 확인이 있어 type으로 구분절차
	var htmlSource = "";
	var non_viewTxt = "";
	var non_viewTime = "";
	var dt = new Date();
	var time = zeroAdd(dt.getHours()) +":"+ zeroAdd(dt.getMinutes());
	if(type == 1){
		non_viewTxt = "진행 중인 회의가 없습니다.";
		if(data.resvusetimefromnext == ""){
			non_viewTime = time + " ~"; // 현재시간 ~
		} else {
			non_viewTime = time + " ~ "+timeView(data.resvusetimefromnext); // ~ 다음 예약 시작 시간
		}
	} else {
		non_viewTxt = "예정 된 회의가 없습니다.";
	}
	if(mode == "S"){
		htmlSource += "<div class='none_reser'><p class='none_tit'>"+non_viewTxt+"</p><span class='offMeeting'>"+non_viewTime+" 회의없음 </span></div>";	
	} else {
		htmlSource += "<div class='none_reser'><p class='none_tit'>"+non_viewTxt+"</p><span class='two_offMeeting'>"+non_viewTime+" 회의없음 </span></div>";
	}
	return htmlSource;
}
function dataError(){
	return "<div class='none_reser'><p class='none_tit'>데이터를 불러오고 있습니다.</p></div>";
}
function netError(){
	return "<div class='none_reser'><p class='none_tit'>네트워크 상태가 불안정합니다.</p></div>";
}

