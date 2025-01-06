# 네고왕 출연을 기다리는 김치 쇼핑몰 "이가네"
<img src="https://github.com/user-attachments/assets/3d53d430-3899-4fc9-af0b-e05c07ac1153" width="400" height="300">


</br>

</br>


### 🎯 목표
  - **많은 주문 및 결제 요청을 감당할 수 있는 서버 구축**: "이가네" 가 네고왕에 출연했다고 가정했을 때 많은 트래픽을 감당할 수 있는 서버를 구축


</br>

### 📚 기술 스택
  - Java , Spring Boot , JPA , MySQL , Redis , Kafka 

</br>


</br>


### 🔗 주요 기능  

  #### 1️⃣ 회원 및 인증 관리 시스템
  - Kakao , Naver 소셜 로그인을 통해 회원 가입 및 로그인을 할 수 있습니다.

  #### 2️⃣ 주문 및 결제
  - 회원은 구매하고자 하는 김치를 장바구니에 담을 수 있습니다.
  - Toss Payments API 를 통한 결제 기능을 제공합니다.
    
  #### 3️⃣ 정산 처리 및 장부 기입
  - 정상적으로 결제가 완료되면 판매자 지갑에 총 정산해야할 금액을 계산하고 업데이트 합니다.
  - 정상적으로 결제가 완료되면 복식부기 방식으로 판매자의 구매자 정보를 모두 DB 에 저장합니다.

  #### 4️⃣ 최저가 검색 기능
  - Naver Open API 를 사용하여 타 쇼핑몰의 김치 상품과 가격을 비교할 수 있습니다.
  
</br>

### 🌈 개선 사항 및 트러블 슈팅

  #### 1️⃣ 이벤트 김치 상품 검색 조회 성능 향상 - <ins>실행 시간 약 99% 개선</ins>

  </br>

  **문제 상황**
  - 네고왕 출연으로 이벤트를 진행하는 김치를 검색해서 조회할 때, TPS 가 높아지는 경우 조회 성능이 매우 느려지는 것을 확인

  **문제 해결**
  - 김치 상품 이름에 데이터베이스 인덱싱을 사용하여 조회 성능을 향상


  **결과**
  ![image](https://github.com/user-attachments/assets/3a41815b-f61c-497a-8d08-41f7c1764c13)

  **결론 및 성과**
  - 평균 TPS 200 환경에서 평균 응답속도 3.5초 -> 0.0089초로 개선
  - 추가적인 인프라 비용 없이 사용하고 있던 DB 계층에서 조회 성능 향상

</br>  

  #### 2️⃣ 결제를 어떻게든 완료시키기 - <ins>동시성 제어 및 응답속도 94.32% 개선</ins>

</br>
  
  **문제 상황**
  - 결제 처리 , 정산 처리 , 장부 기입을 하나의 트랜잭션 안에서 모두 수행할 때, 결제는 정상적으로 처리 됐지만 정산 처리 도중 문제가 발생하면 결제까지 롤백 되는 문제
  - 정산 처리와 같은 서브기능의 문제로 결제라는 메인 기능까지 롤백 되버리는 문제는 수익에 있어서도 큰 문제가 될 것 이라고 판단하여 문제를 해결하기로 결정
  

  ![스크린샷 2025-01-06 오후 8 54 27](https://github.com/user-attachments/assets/3114b2e1-9f25-47db-83a4-d073f19db4f2)

  </br> </br>

  **첫 번째 문제 해결 : @Transactional 전파 옵션 사용**
  - 결제 처리 , 정산 처리 , 장부 기입을 @Transactional 전파 옵션 중 REQUIRES_NEW 를 사용하여 다른 트랜잭션으로 분리 시키고, 정산 처리에서 문제가 발생하더라도 결제 처리 로직에서 해당 Error 를 처리하여 결제를 성공시키도록 함
  - 실패된 정산 처리는 개발자가 로깅을 통해 문제를 해결하도록 함
    
  ![스크린샷 2025-01-06 오후 8 52 52](https://github.com/user-attachments/assets/1bdeaeca-527d-4fe1-a1f6-b267019833cb)

  </br>

  **첫 번째 문제 해결 방식의 문제 상황**
  - @Transactional 전파 옵션을 사용했지만 MySQLTransactionRollbackException(Lock wait timeout exceeded) 발생
  - 발생한 예외는 주로 트랜잭션이 너무 오래 걸리거나 동시에 너무 많은 트랜잭션이 동일한 리소스를 잠그려고 할 때 발생
  

  <img width="1686" alt="스크린샷 2025-01-06 오후 9 22 44" src="https://github.com/user-attachments/assets/514eacb0-3557-4071-8d1d-473dce8f935a" />

  </br> 
  
  - 결제 처리(TransactionA) 완료 후 정산 처리(TransactionB) 를 하기 위해 새로운 트랜잭션을 만들 때 기존 트랜잭션(TransactionA) 는 잠시 중단된 상태로 TransactionB 로 넘어가게 됩니다.
  - 즉 TransactionA 에서 바뀐 변경사항은 현재 커밋되지 않은 상태로 TransactionB 로 넘어가게 되는데, 이때 변경되는 데이터에는 쓰기 락(Exclusive Lock) 이 걸려있는 상태입니다.
  - 이때 TransactionB 에서 같은 데이터를 수정하려고 하기 때문에 락 충돌이 일어나게 되고 대기 상태에 있다가 Lock Wait Timeout 오류가 발생하게 됩니다.

  </br> </br>

  **두 번째 문제 해결 : EntityManager.flush() 사용**
  - 결제 처리가 완료된 후 EntityManger.flush() 를 사용한 후 정산 처리를 하도록 로직을 수정
  - 변경사항이 바로 DB 에 반영이 되기 때문에 Lock 문제가 없을거라고 판단

  </br> </br>
  **두 번째 문제 해결 방식의 문제 상황**
  - EntityManager.flush() 를 사용했지만 똑같이 MySQLTransactionRollbackException(Lock wait timeout exceeded) 발생
  
  ![스크린샷 2025-01-06 오후 9 35 26](https://github.com/user-attachments/assets/469cd977-b43a-463b-a443-6ec934b5a77a)

  </br> 

  - flush() 는 DB 에만 반영되고 TransactionA 자체는 커밋되지 않기 떄문에 해당 데이터에 계속 Lock 이 걸려있는 상태이므로 똑같은 오류가 발생

  </br> </br>

  **세 번째 문제 해결 : Spring Event 사용**
  - Spring Event 기반 비동기 처리를 통해 트랜잭션 경계를 명확히 분리하고, 결제 처리와 후속 작업(정산처리 , 장부기입)을 독립적으로 처리
  
  ![스크린샷 2025-01-06 오후 9 50 40](https://github.com/user-attachments/assets/9227bd23-1df3-4122-b5b7-706ad11cea69)

  </br> 

  - 이벤트 기반으로 처리하면서 동시성 문제와 트랜잭션 락 충돌을 방지

  **성능 개선 : Kafka 를 통한 메시지 처리**
  - Spring Event는 기본적으로 동일 애플리케이션 내부에서 동작하며, 이벤트를 처리하는 리스너가 실행되는 동안 호출한 쓰레드는 블로킹 상태가 되기 때문에 TPS 가 높은 환경에서는 병목 현상을 초래
  - Spring Event는 단일 애플리케이션 컨텍스트 내부에서 작동하기 때문에 다수의 인스턴스나 분산 환경에서는 확장성이 제한되기 때문에 네고왕 이벤트 시 대규모 트래픽을 처리하는데 비효율적이라고 판단
  - 또한 이벤트 메시지가 휘발성 메모리 기반으로 동작하기 떄문에 시스템이 중단되거나 장애가 발생하면 이벤트가 유실될 위험이 있음
  - Spring Event 가 아닌 Kafka 를 활용한 이벤트 기반 아키텍처로 전환

  </br> </br>

  **결과**

  ![스크린샷 2025-01-06 오후 10 04 01](https://github.com/user-attachments/assets/af6dab61-ad56-4204-81b5-09788e1e9dc8)

  - Kafka 를 사용한 EDA 로 전환함으로써 TPS 250 기준 평균 응답 속도 8.8초 -> 0.5초로 개선

</br>  

  #### 3️⃣ 외부 API 와 장애 격리 시키기 - <ins>서킷브레이크 패턴 적용</ins>

  **문제 상황**
  - Naver Open API 를 통해 최저가 김치들을 조회할 수 있는 기능에서 Naver Open API 에 문제가 생기는 경우 "이가네" 서비스에도 장애가 발생할 수 있음을 인지
    
  <img width="765" alt="스크린샷 2025-01-06 오후 10 50 28" src="https://github.com/user-attachments/assets/0a5cb082-89f6-40a9-8bf1-bd8201fd1f94" />

  </br> </br>

  **문제 해결**
  - Resilience4j 를 사용하여 서킷브레이크 패턴을 적용
  - 하루에 한 번 Redis 에 각 김치 종류(배추김치 , 열무 , 깍두기 , 파김치)에 해당하는 최저가 김치들을 Redis 에 캐싱 해놓고, Naver Open API 에 문제가 발생했을 시 캐시된 데이터를 보여주도록 설계

    <img width="681" alt="스크린샷 2025-01-06 오후 10 52 54" src="https://github.com/user-attachments/assets/6c3ed922-8155-4e88-9e8e-5333509a2f00" />

  **결과**
  - Naver Open API에 장애가 생기더라도 해당 장애가 "이가네" 서비스에 전파가 되지 않도록 하여 서비스의 안정성 향상


</br>
