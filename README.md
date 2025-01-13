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

---

  #### 2️⃣ 멀티스레드 환경에서 결제 API 호출 시 Dead Lock 문제 해결 - <ins>멀티스레드 환경에서도 안전한 결제 시스템 구축</ins>
  
</br>  

  **문제 상황**
  - 네고왕 이벤트를 대비하여 부하테스트를 진행할 때, 결제 API 테스트에서 DeadLock 이 발생하여 결제가 되지 않는 문제점을 확인
  - 문제 원인을 파악하기 위해 MySQL InnoDB Log 를 확인
  - 결제 완료 시 PaymentOrder 상태를 EXECUTING -> SUCCESS 로 변환하는 로직이 있고, 이 상태를 변경하기 위해 order_id 를 사용하여 update 쿼리를 사용
  - InnoDB 는 Update 쿼리에도 Lock 을 걸게 되는데 where 에 오는 컬럼이 인덱스가 아니라면 모든 행을 확인해야하는데, 이때 Full Table Scan 이 일어나면서 모든 행을 잠금
  - 멀티스레드 환경에서는 이 InnoDB 동작 원리 때문에 Dead Lock 발생

</br>  

  **문제 상황을 그림으로 표현**
  <img width="1449" alt="스크린샷 2025-01-07 오전 12 31 52" src="https://github.com/user-attachments/assets/5c5d78cc-067e-42e6-9b63-e8f4a075df8a" />

  

  - TransactionA 가 첫 번째 레코드에 대한 잠금을 획득 하고 다음 레코드로 진행을 할 때 TransactionB 도 테이블 스캔을 시도하며 첫 번째 레코드에  잠금을 요청한다. (TransactionB -> TransactionA)
  - TransactionA 도 이후 두 번째 레코드에 대한 잠금을 요청하지만 TransactionB 가 이미 보유한 잠금이기 때문에 대기 상태로 전환 (TransactionA -> TransactionB)
  - 두 Transaction 이 서로의 잠금을 기다리는 순환 대기 상태가 발생하여 DeadLock 이 발생한다.

</br>  

  **문제 해결**
  - PaymentOrder Table 의 index 에 order_id 도 추가하여 update 쿼리를 실행할 때 모든 레코드를 잠그지 않도록 하여 문제를 해결
  

  **결론 및 성과**
  - 멀티스레드 환경에서도 Dead Lock 이 발생하지 않고 정상적으로 결제 API 동작
    
---

  #### 3️⃣ 주문 및 결제 성능 개선 - <ins>응답속도 31.6% 개선</ins>

</br>  

  **문제 상황**
  - 네고왕 이벤트 시 높은 TPS가 예상되었고, TPS 30인 상황에서 평균 응답 시간이 약 7초까지 증가해 사용자 만족도와 서비스 품질에 부정적인 영향을 미칠 가능성을 확인.
  - 이를 해결하기 위해 추가 비용이 들지 않고 설정 변경 만으로도 쉽게 구현이 가능한 Thread 튜닝으로 최적화를 하기로 결정
  - Tomcat Thread , HikariCP Thread 수를 튜닝하면서 자원 활용도를 최적화

</br>  

  **문제 해결(TPS 20 환경)**
  - 처음에 Tomcat , HikariCP 쓰레드 튜닝을 하지 않고 TPS 20 환경에서 부하테스트를 하면서 모니터링을 한 결과 기본 Tomcat Thread Pool 사용량이 매우 낮은것을 파악하여 기본값이 200개인 Thread 를 낮춰서 자원을 아끼기로 함
  - Tomcat Thread Max 개수를 튜닝하고 성능 테스트를 하면서 최적의 Thread 개수를 찾음

  </br>  

  **성능 테스트 결과**
  </br>  
  <img width="718" alt="스크린샷 2025-01-10 오전 12 30 35" src="https://github.com/user-attachments/assets/02f53bae-c1d6-487c-a496-16b80ec3bed4" />

  </br>  

  **문제 해결(TPS 30 환경)**
  - 똑같이 처음에는 Tomcat , HikariCP 쓰레드 튜닝을 하지 않고 TPS 30 환경에서 부하테스트를 하면서 모니터링을 한 결과 기본 Tomcat Thread Pool 사용량은 80% 로 적당하지만 HikariCP 에서 대기 요청 하는 Thread 수가 많아지는 것을 확인
  - 그래서 HikariCP 의 maximum-pool-size 를 늘려가면서 부하 테스트를 진행하여 최적의 maximum-pool-size 개수를 찾음

  </br>  
  
  **성능 테스트 결과**
  </br>  
  <img width="635" alt="스크린샷 2025-01-10 오전 12 25 36" src="https://github.com/user-attachments/assets/92c1580d-82eb-490d-9e1e-1f94d2ce8f21" />

  </br>  

  **결론**
  - HikariCP와 Tomcat의 쓰레드 풀을 최적화할 때, 과도한 쓰레드 수로 지정하게 되면 컨텍스트 스위칭 비용도 증가하고 시스템 자원을 과도하게 소모하여 오히려 응답 속도가 느려짐
  - I/O-바운드 작업을 고려하여 CPU 코어 수를 초과하는 스레드 생성 및 성능 테스트를 통해 최적의 스레드 수 찾아야 함

---

  #### 4️⃣ 결제를 어떻게든 완료시키기 - <ins>동시성 제어 및 응답속도 94.32% 개선</ins>  

</br>
  
  **문제 상황**
  - 결제 처리 , 정산 처리 , 장부 기입을 하나의 트랜잭션 안에서 모두 수행할 때, 결제는 정상적으로 처리 됐지만 정산 처리 도중 문제가 발생하면 결제까지 롤백 되는 문제
  - 정산 처리와 같은 서브기능의 문제로 결제라는 메인 기능까지 롤백 되버리는 문제는 수익에 있어서도 큰 문제가 될 것 이라고 판단하여 문제를 해결하기로 결정

</br> </br>
  
  **문제 상황을 그림으로 표현**
  ![스크린샷 2025-01-06 오후 8 54 27](https://github.com/user-attachments/assets/3114b2e1-9f25-47db-83a4-d073f19db4f2)

  </br> </br>

  

  **첫 번째 문제 해결 : @Transactional 전파 옵션 사용**
  - 결제 처리 , 정산 처리 , 장부 기입을 @Transactional 전파 옵션 중 REQUIRES_NEW 를 사용하여 다른 트랜잭션으로 분리 시키고, 정산 처리에서 문제가 발생하더라도 결제 처리 로직에서 해당 Error 를 처리하여 결제를 성공시키도록 함
  - 실패된 정산 처리는 개발자가 로깅을 통해 문제를 해결하도록 함
    
  ![스크린샷 2025-01-06 오후 8 52 52](https://github.com/user-attachments/assets/1bdeaeca-527d-4fe1-a1f6-b267019833cb)

  </br>

  **첫 번째 문제 해결 방식 중 발생한 이슈**
  - @Transactional 전파 옵션을 사용했지만 MySQLTransactionRollbackException(Lock wait timeout exceeded) 발생
  - 발생한 예외는 주로 트랜잭션이 너무 오래 걸리거나 동시에 너무 많은 트랜잭션이 동일한 리소스를 잠그려고 할 때 발생
  

  **문제 상황을 그림으로 표현**
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
  **두 번째 문제 해결 방식 중 발생한 이슈**
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

  </br> 

  **성능 개선 : Kafka 를 통한 메시지 처리**
  - Spring Event는 기본적으로 동일 애플리케이션 내부에서 동작하며, 이벤트를 처리하는 리스너가 실행되는 동안 호출한 쓰레드는 블로킹 상태가 되기 때문에 TPS 가 높은 환경에서는 병목 현상을 초래
  - Spring Event는 단일 애플리케이션 컨텍스트 내부에서 작동하기 때문에 다수의 인스턴스나 분산 환경에서는 확장성이 제한되기 때문에 네고왕 이벤트 시 대규모 트래픽을 처리하는데 비효율적이라고 판단
  - 또한 이벤트 메시지가 휘발성 메모리 기반으로 동작하기 떄문에 시스템이 중단되거나 장애가 발생하면 이벤트가 유실될 위험이 있음
  - Spring Event 가 아닌 Kafka 를 활용한 이벤트 기반 아키텍처로 전환

  </br> </br>

  **결과**

  ![스크린샷 2025-01-06 오후 10 04 01](https://github.com/user-attachments/assets/af6dab61-ad56-4204-81b5-09788e1e9dc8)
  - Kafka 를 사용한 EDA 로 전환함으로써 TPS 250 기준 평균 응답 속도 8.8초 -> 0.5초로 개선

---

  #### 5️⃣ 외부 API 와 장애 격리 시키기 - <ins>서킷브레이크 패턴 적용</ins>  

  **문제 상황**
  - Naver Open API 를 통해 최저가 김치들을 조회할 수 있는 기능에서 Naver Open API 에 문제가 생기는 경우 "이가네" 서비스에도 장애가 발생할 수 있음을 인지

  **문제 상황을 그림으로 표현**
  
  <img width="765" alt="스크린샷 2025-01-06 오후 10 50 28" src="https://github.com/user-attachments/assets/0a5cb082-89f6-40a9-8bf1-bd8201fd1f94" />

  </br>

  **문제 해결**
  - Resilience4j 를 사용하여 서킷브레이크 패턴을 적용
  - 하루에 한 번 Redis 에 각 김치 종류(배추김치 , 열무 , 깍두기 , 파김치)에 해당하는 최저가 김치들을 Redis 에 캐싱 해놓고, Naver Open API 에 문제가 발생했을 시 캐시된 데이터를 보여주도록 설계

  **서킷브레이크 패턴에서 사용한 설정**
  ```yaml
  resilience4j.circuitbreaker:
  configs:
    default:
      slidingWindowType: COUNT_BASED
      minimumNumberOfCalls: 7
      slidingWindowSize: 10
      waitDurationInOpenState: 10s
      failureRateThreshold: 40
      slowCallDurationThreshold: 3000
      slowCallRateThreshold: 60
      permittedNumberOfCallsInHalfOpenState: 5
      automaticTransitionFromOpenToHalfOpenEnabled: true
  ```
  1. slidingWindowType: COUNT_BASED
     - COUNT_BASED 는 요청 수가 적을 때도 명확히 동작하기 때문에 COUNT_BASED 로 설정
    
  2. minimumNumberOfCalls: 7
     - 서킷브레이커가 작동하기 위해 최소 7번의 호출이 필요하도록 설정
     - 너무 적은 숫자로 하게 되면 실패율 계산에 왜곡이 될 수 있기 때문에 최소 7번 이상의 호출 데이터를 기반으로 실패 여부를 판단하도록 하기 위해 7 로 설정
  3. slidingWindowSize: 10
     - 서킷브레이커가 최근 10번의 호출을 기준으로 실패율을 계산하도록 설정
     - 최근 호출 데이터만 고려하기 때문에 동적으로 변화하는 상태에 빠르게 반응할 수 있고, 슬라이딩 윈도우 크기가 너무 크면 반응이 느려지고, 너무 작으면 신뢰가 떨어지기 때문에 10 으로 설정

  4. waitDurationInOpenState: 10s
     - 서킷이 열려 있는 상태에서 10초 동안 요청을 차단하도록 설정
     - 너무 짧으면 서버에 불필요한 요청이 유입되고, 너무 길면 복구된 서비스에 대해 빠르게 반응하지 않기 떄문에 10s 로 지정
    
  5. failureRateThreshold: 40
     - 실패율이 40% 를 초과하면 서킷브레이커가 열리도록 설정
     - 지나치게 낮은 임계값은 정상적인 동작에도 서킷이 열릴 수 있기 때문에 지나치게 낮지 않으면서도 문제가 감지 됐을 때 빠르게 대응할 수 있는 40% 로 지정

  6. slowCallDurationThreshold: 3000
     - 3초 이상 걸리는 호출을 "느린 호출" 로 간주하도록 설정
     - 지나치게 짧게 설정하면 느린 호출의 기준이 너무 민감해지고, 너무 길면 성능 문제를 감지하지 못 할 수 있기 때문에 3초로 지정
       
  7. slowCallRateThreshold: 60
     - "느린 호출" 비율이 60% 를 초과하면 서킷브레이커가 열리도록 설정
     - 느린 호출이 전체 호출의 과반수를 초과하면 성능 문제가 발생했음을 나타나기 때문에 50% 로 지정

  8. permittedNumberOfCallsInHalfOpenState: 5
     - 서킷브레이커가 Half-Open 상태일 때 최대 5번의 호출을 허용하도록 설정
     - 호출 수가 너무 많으면 복구를 확인하기 전에 과부하가 발생할 수 있고, 너무 적으면 복구 여부 판단이 어렵기 때문에 5번으로 설정

  9. automaticTransitionFromOpenToHalfOpenEnabled: true
      - 서킷브레이커가 Open 상태에서 자동으로 Half-Open 상태로 전환하도록 설정
      - 자동 전환은 개발자의 수작업 개입을 줄이고, 시스템 복구를 빠르게 확인할 수 있는 장점이 있기 떄문에 true 로 지정
  

</br> 

  <img width="681" alt="스크린샷 2025-01-06 오후 10 52 54" src="https://github.com/user-attachments/assets/6c3ed922-8155-4e88-9e8e-5333509a2f00" />

  **결과**
  - Naver Open API에 장애가 생기더라도 해당 장애가 "이가네" 서비스에 전파가 되지 않도록 하여 서비스의 안정성 향상


</br>
