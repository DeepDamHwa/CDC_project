<br>

<img src="https://capsule-render.vercel.app/api?type=waving&height=250&color=gradient&customColorList=30&text=CDC(Change%20Data%20Chapture)&section=header&fontSize=50&fontAlignY=30&animation=fadeIn&rotate=0&desc=Oracle%20to%20MySQL&descSize=30&reversal=false" style="width: 120%;" />

[//]: # (<img src="https://capsule-render.vercel.app/api?type=soft&height=250&color=gradient&#40;7AC8C2,1C5C7F&#41;&text=CDC&#40;Change%20Data%20Chapture&#41;&section=header&fontSize=60&fontAlignY=30&animation=fadeIn&rotate=0&desc=Oracle%20to%20MySQL&descSize=30&reversal=false" style="width: 120%;" />)
<!-- <br>
<div align="center">
<img src="https://github.com/user-attachments/assets/3f6df515-9e11-4b63-b4e1-c35d22176721" style="width: 60%;">
</div> -->
<br>

## 팀원 소개

[//]: # (> **[한화시스템 BEYOND SW캠프 6기] Final Project**)

> **딥담화 DeepDamHwa**<br>
> 개발 기술스택을 주제로 매주 리뷰를 통해, 기술을 딥하게 학습하는 스터디 입니다. <br>
이 스터디는 실습을 통해 실무에서 적용할 수 있는 경험을 쌓는 것을 목표로 합니다. 
<table align="center">
 <tr>
    <td align="center"><a href="https://github.com/kangkings"><img src="https://github.com/user-attachments/assets/87958dbd-8949-40ee-9d44-94b72a38220f" width="150px;" alt=""></td>
    <td align="center"><a href="https://github.com/706com"><img src="https://github.com/user-attachments/assets/af86835f-63f8-470f-8a67-41d67b162a3d" width="150px;" alt=""></td>
    <td align="center"><a href="https://github.com/jimnyy"><img src="https://github.com/user-attachments/assets/0525e556-b443-4e9c-bf02-fc1820eae111" width="150px;" alt=""></td>
    <td align="center"><a href="https://github.com/shinebyul"><img src="https://github.com/user-attachments/assets/2e27dee2-7500-4ff1-ba30-62fe7b36de27" width="150px;" alt=""></td>
     <td align="center"><a href="https://github.com/SihyunSeo"><img src="https://github.com/user-attachments/assets/b2fdab43-376c-476e-9e60-f240a04a9d4c" width="150px;" alt=""></td>
  </tr>
  <tr>
    <td align="center"><a href="https://github.com/kangkings"><b>강태성</b></td>
    <td align="center"><a href="https://github.com/706com"><b>곽동현</b></td>
    <td align="center"><a href="https://github.com/jimnyy"><b>도지민</b></td>
    <td align="center"><a href="https://github.com/shinebyul"><b>한별</b></td>
    <td align="center"><a href="https://github.com/SihyunSeo"><b>서시현</b></td>
  </tr>
  </table>
<br>


### 목차
- [기술 스택](#-기술-스택)
- [CDC 프로젝트 소개](#%EF%B8%8E%EF%B8%8E-cdc-project-소개)
- [프로젝트 설계](#-프로젝트-설계)
- [시스템 개선 과정](#-시스템-개선-과정)
- [문제 해결 사례](#-문제-해결-사례)
  <br><br>


## 🔗 기술 스택
#### &nbsp;　[ Backend ]
&nbsp;&nbsp;&nbsp;&nbsp; 
<img src="https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white" style="border-radius: 5px">
<img src="https://img.shields.io/badge/Spring Boot-6DB33F?style=for-the-badge&logo=Spring Boot&logoColor=white" style="border-radius: 5px;">
<img src="https://img.shields.io/badge/Spring data jpa-6DB33F?style=for-the-badge&logo=Spring Boot&logoColor=white" style="border-radius: 5px;">
<img src="https://img.shields.io/badge/Spring Batch-6DB33F?style=for-the-badge&logo=Spring&logoColor=white" style="border-radius: 5px;">

#### &nbsp;　[ DB ]
&nbsp;&nbsp;&nbsp;</a>
<img src="https://img.shields.io/badge/mysql-4479A1.svg?style=for-the-badge&logo=mysql&logoColor=white" style="border-radius: 5px">
<img src="https://img.shields.io/badge/Oracle-F80000?style=for-the-badge&logo=oracle&logoColor=white" style="border-radius: 5px">

#### &nbsp;　[ SERVER ]
&nbsp;&nbsp;&nbsp;
<img src="https://img.shields.io/badge/docker-2496ED?style=for-the-badge&logo=docker&logoColor=white" style="border-radius: 5px;">
<img src="https://img.shields.io/badge/kafka-231F20?style=for-the-badge&logo=apachekafka&logoColor=white" style="border-radius: 5px;">
<img src="https://img.shields.io/badge/Ubuntu-E95420?style=for-the-badge&logo=ubuntu&logoColor=white" style="border-radius: 5px;">

<br><br>
## ︎︎🔗 CDC Project 소개

### 프로젝트 배경
딥담화 기술 리뷰에서 다뤘던 주제 **Data Migration (Oracle to MySQL)**<br>
초기에는 대량 데이터를 옮기는 가장 일반적인 방법인 "덤프-로드 방식"을 활용하여 마이그레이션을 진행했다.<br>
이 방식은 OracleDB 데이터를 한 번에 덤프한 후 MySQL로 로드하는 단순한 과정으로, 구현이 비교적 쉬운 장점이 있다.

그러나 실제로 마이그레이션을 수행하는 과정에서 다음과 같은 **문제점**들을 발견하였다.

[//]: # (**1. 서버 다운타임 발생:**)
[//]: # (>)
[//]: # (>- 기존 데이터를 덤프하고 로드하는 동안 가적으로 발생한를 중단해야 했기 때문에 다운타임 .)

>**1. 실시간 데이터 동기화 부족:**
>
>- 마이그레이션이 진행되는 동안 새로 삽입되거나 수정된 데이터가 누락될 가능성이 있었다.

>**2. 데이터 일관성 문제:**
>
>- 덤프 시점과 로드 완료 시점 간의 시간차로 인해 데이터의 최신 상태를 보장하기 어려웠다. <br>

<br>

이러한 문제점들로 인해 서버 다운타임을 최소화하면서 마이그레이션을 수행할 방법에 대해 고민하게 되었고, 실시간으로 데이터 변경 사항을 추적하고 동기화할 수 있는 **CDC(Change Data Capture)** 환경을 설계하고 구축하는 것을 목표로 삼았다.
  
CDC 방식을 **선택하게 된 이유**는 다음과 같다.

>**1. 실시간 데이터 동기화 가능:**
>
>- 기존 "덤프-로드 방식"은 마이그레이션 중 추가로 생성되거나 수정되는 데이터를 다룰 수 없었다. CDC는 변경 로그를 기반으로 데이터 변경 사항을 추적할 수 있어 이러한 한계를 극복할 수 있었다.

>**2. 서비스 연속성 유지:**
>
>- CDC를 통해 실시간으로 데이터를 동기화하면 서비스 중단 없이 마이그레이션을 진행할 수 있었다.

>**3. 데이터 일관성 보장:**
>
>- 덤프와 로드 간의 시간차로 인해 데이터 불일치가 발생할 가능성이 있었지만, CDC는 데이터베이스 변경 로그를 실시간성으로 감지하여 최신 데이터를 지속적으로 동기화함으로써 데이터 일관성을 유지할 수 있었다.

>**4. 확장성과 유연성:**
>
>- CDC 시스템은 데이터베이스 변경 사항을 메시징 시스템(Kafka 등)으로 전달해 다양한 데이터베이스나 애플리케이션과 연동할 수 있었다. 이를 통해, 향후 시스템 확장에도 유연하게 대응할 수 있는 기반을 마련할 수 있었다.


  <br>

###  프로젝트 목표
실시간으로 삽입, 수정, 삭제가 발생하는 Oracle 데이터의 로그를 수집하여, MySQL에 적합한 데이터로 전처리 후 MySQL에 적용하는 CDC 시스템을 구축.

- `Log Scanner` : Oracle의 REDO_LOG를 수집해 Kafka로 발행. Offset을 활용해 변동 데이터를 지속적으로 추적 및 처리.<br>
- `Data Transformer` : Kafka에 발행된 REDO_LOG를 읽어 Oracle로부터 실제 데이터를 조회 후 MySQL에 적재 가능한 데이터로 변환 작업 후, Kafka로 다시 발행<br>
- `Data Loader` : Kafka에 발행된 변경 데이터를 가져와 MySql DB에 동기화
<br>

[//]: # (### 세부 기능)

[//]: # (- **`WIKI`**<br>)

[//]: # (  유저들이 프로그래밍 언어별 정보, 최신 기술에 관한 지식을 자유롭게 공유하고, 이를 체계적으로 관리할 수 있는 서비스를 제공한다.)

[//]: # (- **`QnA`**<br>)

[//]: # (  개발자들이 직면한 문제와 에러를 질문하고, 답변을 얻을 수 있는 서비스를 제공한다.)

[//]: # (- **`ErrorArchive`**<br>)

[//]: # (  단순히 문제를 해결하는 것에 그치지 않고, 해결 과정을 블로그 형태로 정리하고, 공유한다.)

[//]: # (- **`채팅`**<br>)

[//]: # (  게시글의 작성자에게 추가적인 질문사항이 있을 떄, 실시간 소통이 가능한 1:1채팅 서비스를 제공한다.)

[//]: # (- **`포인트 및 랭킹`**<br>)

[//]: # (  서비스 사용을 유도하기 위해 특정 조건에 따라 포인트를 부여하고, 활동을 통해 본인의 등급 및 랭킹을 제공한다.    )


## 🔗 프로젝트 설계
<img src="https://github.com/user-attachments/assets/a3deb804-8540-464a-91d0-fc1613d1cc86" style="width: 100%;"><br>
#### 시스템 프로세스
1. 사용자에 의해 데이터 변동(삽입,삭제,수정)이 발생한다. (Spring Batch로 구현 - 5분마다 700개의 랜덤 DML생성)
2. Log Scanner 서버가 OracleDB에 저장된 Offset 값을 읽어온다.

3. Oracle의 REDO_LOG로부터 조회 된 Offset 값 이후의 로그를 조회해 Kafka로 발행한다.(topic: change_log)
   - 이때, 현재 활성화 된 REDO_LOG 파일 버전을 확인하기 위해, 저장된 REDO_LOG version과 현재 활성화된 REDO_LOG version을 비교한다.  
   &nbsp;&nbsp;➔ 만약 version이 같다면, 현재 활성화된 REDO_LOG 파일을 LogMiner로 읽어온 후, Offset값 이후의 로그를 조회해 Kafka로 발행한다.  
   &nbsp;&nbsp;➔ 만약 version이 다르다면, Offset에 저장된 값의 REDO_LOG 파일을 시작으로 현재 활성화된 REDO_LOG 파일까지 모든 로그를 조회해 Kafka로 발행한다.   
   - 데이터 조회는 REDO_LOG 파일로 부터 읽은 데이터의 XIDUSN값과, XIDSLT값을 활용해 반드시 commit 된 트랜잭션 데이터만 조회를 진행한다.

4. Data Transformer 서버가 Kafka에 발행된 로그의 ROW_ID값을 이용해 Oracle로부터 실제 데이터를 조회한다.
5. 조회한 데이터와 필요한 정보를 MySQL이 적재 가능한 객체 형태로 담아 Kafka로 발행한다.(topic: payload)
6. Data Loader 서버가 Kafka로부터 데이터를 가져와 MySql에 동기화한다.

<br>

#### 시스템 설계
- [Kafka 사용 이유](https://github.com/DeepDamHwa/CDC_project/wiki/%EC%99%9C-Kafka%EC%9D%B8%EA%B0%80%3F)
- [Spring Batch 사용 이유](https://github.com/DeepDamHwa/CDC_project/wiki/%EC%99%9C-Spring-Batch-%EC%9D%B8%EA%B0%80%3F)


<br><br><br>
## 🔗 시스템 개선 과정
#### 1. 초기 시스템
<div align="center">
    <img src="https://github.com/user-attachments/assets/75fef291-36da-46cf-9c4a-6f9f92d298be" style="width: 70%;"><br>
</div>

>Spring Batch 하나의 서버에 Oracle과 MySql을 모두 연결 및 처리하여, Oracle의 변경 사항을 MySql로 바로 동기화</td>

> **한계** : Spring Batch가 조회, 처리, 저장을 모두 책임지기 때문에, 데이터량이 많아질 경우 Spring Batch에 병목 현상
발생 가능성 높음


[//]: # (Spring Batch 서버에 Oracle과 MySql을 연결해, Oracle의 변경 사항을 읽어 MySql로 바로 동기화.)

[//]: # (**한계** : )

[//]: # (- Spring Batch가 조회, 처리, 저장을 모두 책임지기 때문에, 데이터량이 많아질 경우 Spring Batch에 병목 현상)

[//]: # (발생 가능성 높음)

<br>

#### 2. Kafka + 적재 담당 서버 도입
<img src="https://github.com/user-attachments/assets/03d0d9f0-35df-494b-b4e6-645f6cdaf596" style="width: 100%;"><br>
> Kafka와 적재 담당 서버를 도입해 데이터를 조회하는 역할과 적재하는 역할을 분리

> **개선 사항** : 
> - 기존의 모든 데이터 처리 과정을 부담했던 Spring Batch 서버의 역할을 분리하므로써 부하 감소
> - 데이터 조회와 데이터 적재 과정이 분리되어 유지보수와 장애대응이 용이해짐
> - 데이터 전송은 Kafka에게 위임

> **한계** :
> - Spring Batch가 모든 데이터 처리(로그 읽기, 가공, Kafka 전송)를 단독으로 수행하기 때문에 서버에 부하가 집중됨

<br>

#### 3. 변경 감지 시스템 구조 개선
<img src="https://github.com/user-attachments/assets/a3deb804-8540-464a-91d0-fc1613d1cc86" style="width: 100%;"><br>
> - 로그 데이터 조회 / 실제 ROW 데이터 조회의 역할을 분리함으로써 대용량 처리 용이성 확보
> - 병목 현상 감소
> - 장애 대응 용이
<br>

## 🔗 문제 해결 사례

### [1. 트랜잭션 격리 수준을 고려하지 못한 Log 수집](https://github.com/DeepDamHwa/CDC_project/wiki/%ED%8A%B8%EB%9E%9C%EC%9E%AD%EC%85%98-%EA%B2%A9%EB%A6%AC-%EC%88%98%EC%A4%80%EC%9D%84-%EA%B3%A0%EB%A0%A4%ED%95%9C-%EB%A1%9C%EA%B7%B8-%EC%88%98%EC%A7%91)

**[문제]**
<br>Redo log를 분석하여 데이터를 수집하는 과정에서 트랜잭션 격리 수준을 고려하지 않아, 커밋되지 않은 데이터(테이블에 반영되지 않고 로그에만 존재하는 데이터)가 처리되어 데이터 정합성 문제 발생.
  
**[개선]**
<br>Redo log에서 트랜잭션을 구분할 수 있는 식별 값을 활용하여, 커밋되기 전(Active 상태) 트랜잭션의 데이터를 제외하고 커밋된 데이터만 처리하도록 로직 개선
<br>
### [2. 로그 파일 번호를 고려하지 않은 Offset 관리로 인해 누락 데이터 발생](https://github.com/DeepDamHwa/CDC_project/wiki/Offset%EA%B3%BC-%EB%A1%9C%EA%B7%B8%ED%8C%8C%EC%9D%BC-%EA%B4%80%EB%A6%AC%EB%A5%BC-%ED%86%B5%ED%95%9C-%EB%A1%9C%EA%B7%B8-%EB%8D%B0%EC%9D%B4%ED%84%B0-%EC%88%98%EC%A7%91-%EC%8B%9C%EC%9E%91-%EC%9C%84%EC%B9%98-%ED%99%95%EC%9D%B8)

**[문제]**
<br>여러 개의 로그 파일을 처리해야 할 때, 현재 Offset이 위치한 로그 파일만 처리되어 다른 로그 파일의 데이터가 누락되는 문제가 발생.

**[개선]**
<br>파일 번호별 Offset을 추가하여 파일 단위로 관리하고, 이전 Batch 작업에서 처리한 파일부터 현재 파일까지의 로그를 모두 조회하는 로직으로 데이터 누락을 방지
<br>
### [3. 미처리 트랜잭션이 이후 커밋된 후에도 Kafka로 전송되지 않아 데이터가 누락되는 문제 발생](https://github.com/DeepDamHwa/CDC_project/wiki/%EB%AF%B8%EC%B2%98%EB%A6%AC-%ED%8A%B8%EB%9E%9C%EC%9E%AD%EC%85%98%EC%9D%B4-%EC%9D%B4%ED%9B%84-%EC%BB%A4%EB%B0%8B%EB%90%9C-%ED%9B%84%EC%97%90%EB%8F%84-Kafka%EB%A1%9C-%EC%A0%84%EC%86%A1%EB%90%98%EC%A7%80-%EC%95%8A%EC%95%84-%EB%8D%B0%EC%9D%B4%ED%84%B0%EA%B0%80-%EB%88%84%EB%9D%BD%EB%90%98%EB%8A%94-%EB%AC%B8%EC%A0%9C-%EB%B0%9C%EC%83%9D)

**[문제]**
<br>트랜잭션이 커밋되기 전(Active 상태)이라 Kafka로 전송 제외. 그러나 이후 커밋된 데이터도 전송되지 않아 데이터 누락 문제 발생.

**[개선]**
<br>미처리된 트랜잭션을 ACTIVE_TRANS 테이블에 저장하고, 이후 배치 실행 시 해당 테이블에서 커밋 여부를 재확인하여 커밋된 데이터만 Kafka로 전송하도록 로직 개선
<br>
<br>
### [4. Kafka 토픽 분리로 인한 데이터 정합성 문제 발생](https://github.com/DeepDamHwa/CDC_project/wiki/Kafka-%ED%86%A0%ED%94%BD-%EB%B6%84%EB%A6%AC%EB%A1%9C-%EC%9D%B8%ED%95%9C-%EB%8D%B0%EC%9D%B4%ED%84%B0-%EC%A0%95%ED%95%A9%EC%84%B1-%EB%AC%B8%EC%A0%9C-%EB%B0%9C%EC%83%9D)

**[문제]**
<br>Data Loader에서 토픽 처리 과정에서, 테이블별로 발행된 Kafka 토픽이 병렬 처리되면서 DML 순서 불일치로 데이터 정합성 문제가 발생함.

**[개선]**
<br>모든 DML 이벤트를 단일 Kafka 토픽으로 통합하여 순서 보장을 유지한 상태로 발행하고, 각 테이블에서 필요한 정보를 파싱하여 데이터베이스에 순차적으로 반영함.
<br>
<br><br>
## 🔗 계획 - 추가 개선 사항

### [이벤트 처리 실패로 인한 데이터 정합성 문제](https://github.com/DeepDamHwa/CDC_project/edit/main/README.md)

**[문제]**
<br>kafka event 처리가 실패한 경우, 해당 데이터에 관한 데이터가 반영되지 않아 데이터 정합성 발생

**[예상 개선 사항]**
<br>실패 이벤트를 별도의 토픽으로 발행하여, 로그에 기록한다.
