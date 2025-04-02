# 네고왕 출연을 기다리는 김치 쇼핑몰

</br>


<img width="300" alt="스크린샷 2024-12-27 00 24 13" src="https://github.com/user-attachments/assets/612f3636-e5c4-454a-854b-a70530695e65" />

</br>

### 🔖 프로젝트 개요
  - **주제** : 네고왕 출연으로 인한 대규모 트래픽 환경을 가정하여, 높은 동시 사용자 요청에도 안정적인 결제 시스템과 고성능 백엔드 인프라를 구축한 김치 쇼핑몰 프로젝트
  
</br>

### 🎯 목표
  - **실제 이커머스 서비스와 유사한 시스템 구현**: 결제 이후 판매자들 정산을 위한 정산 처리와 비즈니스의 신뢰성을 위한 복식부기 형태의 장부 기입 처리까지 구현
  - **동시 접속 트래픽 환경에서도 안정적인 결제 서비스 기획**: 최대한 많은 사용자들의 주문 및 결제를 받아 많은 수익을 창출



</br>

### 📚 기술 스택
- Backend : Spring Boot , Spring Data JPA , Spring Event , Kafka , Resilience4j
- DB : MySQL
- 기타 : TossPayment API , Naver API , Artillery

</br>

### 🔗 주요 기능  

  #### 1️⃣ 회원 및 인증 관리 시스템
  - 네이버 , 카카오 소셜 로그인을 통해 회원 가입을 할 수 있습니다.
  
  #### 2️⃣ 주문 및 결제
  - 장바구니에 사고자 하는 상품을 담을 수 있습니다.
  - TossPayment API를 통해 결제할 수 있습니다.
  
  #### 3️⃣ 정산 처리 및 장부 기입
  - 결제가 완료되면, 해당 판매자의 지갑 잔액에 판매 금액이 자동으로 반영됩니다.
  - 동시에, 복식부기 형식으로 장부를 기록하여 비즈니스의 신뢰성과 재무 투명성을 확보합니다.

  #### 4️⃣ 최저가 김치 비교
  - Naver 쇼핑 API를 활용해, 다른 김치 상품과의 가격을 비교할 수 있습니다.

</br>

### :hammer_and_wrench: ERD


<img width="1281" alt="스크린샷 2025-04-02 오후 11 01 15" src="https://github.com/user-attachments/assets/e5705b92-97a0-486a-8958-047e71a1dd05" />

### 💣 Artillery (부하테스트) 스크립트 (테스트 환경 : MacBook M1 - 로컬 환경)

```yaml
config:
  target: 'http://localhost:8081'
  phases:
    - duration: [시간]
      arrivalRate: [요청 횟수]
      name: Constant Load
  payload:
    path: "ids.csv"
    fields:
      - "memberId"
  plugins:
    faker: {}

scenarios:
  - flow:
      - post:
          url: "/api/check-out"
          json:
            cartId: 1
            kimchiIds:
              - 1
              - 2
              - 3
            buyerId: 1
            seed: "{{ date.now }}"
          capture:
            - json: "$.result.orderId"
              as: "orderId"

      - post:
          url: "/test/confirm"
          json:
            paymentKey: "{{ faker.random.uuid }}"
            orderId: "{{ orderId }}"
            memberId: "{{ memberId }}"
            amount: 38000
```

</br>

### 🌈 트러블 슈팅 및 개선 사항

  #### 1️⃣ 멀티스레드에서 발생하는 DeadLock 문제 식별 및 해결 

  </br>

  **문제 상황**
  - 싱글스레드 환경에서는 정상적으로 동작하는 것을 확인하고 Artillery를 사용하여 멀티스레드 환경을 가정하여 결제 API 성능 테스트를 진행했고 그 결과 DeadLock 문제가 발생했습니다.

  **문제 원인**
  - 결제가 완료된 후 결제 상태를 변경하는 UPDATE 쿼리가 실행되는데 WHERE 절에 사용되는 Column이 Index 로 설정되어 있지 않아 Full Table Scan이 발생이 발생했고, 이로 인해 모든 행이 잠기면서 DeadLock 문제가 발생하였음을 확인할 수 있었습니다.

  **문제 해결**
  - UPDATE 쿼리에서 WHERE 절에 사용되는 Column을 Index로 설정하여 모든 행에 잠금을 걸지 않고 Index 를 통해 필요한 행을 빠르게 찾도록 하여 문제를 해결할 수 있었습니다.

  **결과**
  - 결제 API가 멀티스레드 환경에서도 안정적으로 동작하도록 개선했습니다. 멀티스레드 환경을 고려한 테스트의 중요성을 깨닫고, InnoDB 의 UPDATE 쿼리 처리 과정에서 Index가 없을 경우 발생할 수 있는 Full Table Scan과 이어지는 DeadLock 문제 원인에 대해서 이해하게 됐고 인덱스 설계의 필요성을 알게 됐습니다.
    
---

  #### 2️⃣ 결제를 어떻게든 완료시키기 - 첫 번째 방법(@Transactional 전파 옵션)

</br>
  
  **문제 상황**
  - 결제 API에서는 결제가 성공하면 정산 처리와 장부 기입 로직을 실행합니다.
  - 이때 결제는 정상적으로 완료되었더라도 정산 처리 중 문제가 발생하면 결제까지 롤백되는 문제가 발생했습니다. 
  - 많은 수익을 내기 위해서는 정산 처리 및 장부 기입에 문제가 생기더라도 성공한 결제는 완료시켜야 했습니다.
    
  **문제 원인**
  - 결제 , 정산 처리 , 장부 기입 이 모든 로직이 하나의 트랜잭션 안에서 처리하기 때문에 발생한 문제였습니다.

  **문제 해결**
  - @Transactional 전파 옵션 중 REQUIRES_NEW 를 사용하여 각 로직을 서로 다른 트랜잭션으로 분리 시키고,정산 처리에서 문제가 발생하더라도 결제 처리 로직에서 해당 문제를 처리하여 결제를 성공시키도록 했습니다.

---

  #### 3️⃣ 결제를 어떻게든 완료시키기 - 첫 번째 방법(@Transactional 전파 옵션)의 한계점
  
</br>

  **첫 번째 방법에서 발생한 문제**
  - @Transactional 전파옵션 중 REQUIRES_NEW를 사용하는 첫 번째 방법을 사용하여 멀티스레드 환경에서 테스트를 해본 결과 MySQLTransactionRollbackException이 발생했습니다.

  **문제 원인**
  - 결제 처리를 담당하는 트랜잭션을 TransactionA, 정산 처리를 담당하는 트랜잭션을 TransactionB 라고 가정하겠습니다.
  - 결제 처리가 완료된 후 정산 처리를 진행하기 위해 TransactionA는 잠시 중단되고, TransactionB로 제어가 넘어갑니다.
  - 이때, TransactionA에서 변경한 데이터는 아직 커밋되지 않은 상태로 남아 있으며, 해당 데이터에는 쓰기 락(Exclusive Lock)이 걸려 있습니다.
  - 만약 TransactionB가 같은 데이터를 수정하려고 시도하면, 락 충돌이 발생해 대기상태에 빠지게 됩니다.

  **문제 해결 시도**
  - TransactionA 에서 entityManager.flush()를 강제로 호출하여 변경사항을 즉각 DB에 반영시켜 문제를 해결하려고 했지만 entityManager.flush()는 DB에만 반영되고 TransactionA자체가 커밋되지는 않기 때문에 해당 데이터에 계속 Lock이 걸려 똑같은 오류가 발생합니다.

---

  #### 4️⃣ 결제를 어떻게든 완료시키기 - 두 번째 방법(Spring Event)

  **문제 상황**
  - 결제(TransactionA)에서 데이터 변경 후 커밋되지 않은 상태에서 쓰기 락(Exclusive Lock)이 걸렸고, 정산 처리(TransactionB)가 같은 데이터를 수정하려 시도하면서 충돌이 발생했습니다.
  - entityManager.flush()를 사용해 변경사항을 DB에 반영했지만, 트랜잭션 자체가 커밋되지 않아 락이 유지되었고 동일한 문제가 반복됐습니다.

 **문제 원인**
 - 결제 , 정산 처리 , 장부 기입을 하는 트랜잭션이 명확하게 분리되지 않아 발생하는 문제가 발생한 것 입니다.

 **문제 해결** 
 - Spring Event 기반 비동기 처리를 통해 트랜잭션 경계를 명확하게 분리하고, 결제 처리와 후속 작업(정산 처리 , 장부 기입)을 독립적으로 처리하여 해결했습니다.

 **Spring Event 선택 이유**
 - Spring Event는 Observer 패턴을 기반으로 하여 애플리케이션의 컴포넌트 간에 느슨한 결합을 유지하면서 이벤트를 전달하고 처리할 수 있도록 도와주는 기능입니다.
 - 이를 통해 결제 트랜잭션이 완전히 종료된 이후에 정산 처리 및 장부 기입의 작업이 별도의 흐름으로 안전하게 실행되도록 구성하기 위해 Spring Event를 사용했습니다.

 **결론**
 - 다양한 방법을 시도하며 문제의 원인들을 파악하고, 트랜잭션 경계를 명확히 분리하여 결제를 어떻게든 완료시키는 구조를 설계할 수 있었습니다.
 - 트랜잭션의 범위가 넓어질수록 락 문제와 복잡도가 커지기 때문에 트랜잭션은 짧고 명확해야 한다는 것도 느끼게 됐습니다.
 - 이번 문제 해결을 통해 멀티스레드 , 트랜잭션 , 락에 대한 이해도를 높힐 수있었습니다.

---

  #### 5️⃣ Kafka를 활용한 결제 API 성능 개선하기 - <ins>응답속도 8.8초 -> 0.5초로 94% 개선</ins>

  **문제 상황**
  - Spring Event 기반으로 비동기 처리로 문제를 해결한 뒤 Artillery를 사용해 성능 테스트를 한 결과 TPS 200 에만 도달해도 평균 응답 속도가 5초가 걸리게 됐고 사용자에게 원활한 경험을 위해 성능 개선의 필요성을 느끼게 됐습니다.
  
 **문제 원인**
 - Spring Event 는 기본적으로 애플리케이션 내부(동일 프로세스)에서 이벤트를 발행하고 처리하기 때문에 트래픽이 많아지면 스레드 경쟁 , 메모리 사용량 증가 등으로 인해 성능 병목이 발생하게 된 것입니다.

 **문제 해결** 
 - Kafka 를 도입하여 Event 를 Kafka 토픽으로 발행하도록 하여 성능을 크게 개선했습니다.

 **Kafka 선택 이유**
 - Spring Event는 애플리케이션 내부에서 이벤트를 발행하고 처리하기 때문에, 이벤트 로직이 메인 프로세스와 같은 스레드 풀과 리소스를 공유하게 됩니다.
 - 이로 인해 트래픽이 증가할수록 스레드 경쟁, 메모리 사용량 증가 등으로 성능 병목이 발생했습니다.
 - 반면 Kafka는 외부 메시징 시스템으로, 이벤트를 비동기적으로 큐에 전달하고 별도의 Consumer가 독립적으로 처리합니다.
 - 덕분에 메인 서비스의 부담을 줄이고, 높은 처리량과 빠른 응답 속도를 안정적으로 유지할 수 있어 Kafka를 선택했습니다.

 **결론**
 
 <img width="631" alt="스크린샷 2025-01-26 오후 6 02 42" src="https://github.com/user-attachments/assets/4c5bee36-1f3b-48f8-ab8d-06038c566835" />
 
 - Spring Event 로 처리하던 비동기 로직을 Kafka 를 활용하는 메시징 구조로 전환함으로써, TPS 500 에서도 안정적으로 처리하고 결제 시스템의 성능을 크게 개선할 수 있었습니다.
 - 이번 경험을 통해 트래픽 규모와 서비스 요구사항에 맞춰 아키텍처를 확장해야 한다는 것을 느끼게 됐습니다.

---

  #### 6️⃣ 네이버 API 장애 격리를 위한 서킷브레이커 패턴 도입

  **도입 이유**
  - 네이버 쇼핑 API는 최저가 비교 기능은 외부 시스템이기 때문에, 장애나 지연이 발생했을 때 우리 서비스 전체에 영향을 줄 수 있다는 우려가 있었습니다.
  - 이를 예방하고자 Resilience4j 기반의 서킷 브레이커 패턴을 도입해, 일정 실패율 이상이 감지되면 자동으로 호출을 차단하고 fallback 응답을 제공하는 구조를 만들었습니다.
  
 **설정 값**
 ```yaml
resilience4j.circuitbreaker:
  configs:
    default:
      slidingWindowType: COUNT_BASED
      minimumNumberOfCalls: 20
      slidingWindowSize: 30
      waitDurationInOpenState: 30s
      failureRateThreshold: 40
      slowCallDurationThreshold: 1000
      slowCallRateThreshold: 50
      permittedNumberOfCallsInHalfOpenState: 5
      automaticTransitionFromOpenToHalfOpenEnabled: true
 ```

 **설정 값 근거** 
 
 1. slidingWindowType : COUNT_BASED
    - 네이버 API 를 사용하는 최저가 비교 기능을 사용하는 트래픽은 일정하고 호출 빈도가 안정적이라고 판단하여 COUNT_BASED 로 설정하였습니다.
      
 2. slidingWindowSize : 30
    - 너무 적은 숫자는 빠른 감지에는 유리하지만, 오탐의 확률이 커질 수 있기 때문에 30 으로 지정했고 실제 호출 패턴에 맞춰서 조정할 예정입니다.
      
 3. minimumNumberOfCalls : 20
    - slidingWindowSize 가 30 이기 때문에 약 70% 인 20 으로 설정했습니다.
      
 4. waitDurationInOpenState : 30s
    - 네이버 API 는 오류에 신속하게 대응하기 위해 매 10분 단위로 자동으로 모니터링하고 있기 때문에 30초로 설정했습니다.
      
 5. failureRateThreshold : 40
    - 너무 낮은 값으로 하게 되면 너무 자주 서킷이 열릴 수 있기 때문에 50% 로 설정했습니다.
      
 6. slowCallDurationThreshold : 1000
    - 평균 응답 속도가 0.1초이기 때문에 1초를 초과하게 된다면 느린 호출이라고 판단하기로 했습니다.
      
 7. slowCallRateThreshold : 50
    - 전체 호출의 50% 이상이 느린 호출에 해당한다면 우선 장애로 판단하여 서킷을 열도록 설정했습니다.
      
 8. permittedNumberOfCallsInHalfOpenState : 5
    - 너무 적으면 회복 여부를 정확히 판단하기 어렵고, 너무 많으면 불필요하게 많은 요청을 허용할 수 있기 때문에 5 로 설정했습니다.
      
 9. automaticTransitionFromOpenToHalfOpenEnabled : true
    - false 로 하게 되면 개발자가 직접 HalfOpen 전환을 트리거해야 하기 때문에 true 로 설정
      
 **결론**
 - 서킷브레이커 패턴을 적용해 외부 API 장애를 효과적으로 격리하는 방법을 알게 됐습니다.
 - 또한 다양한 설정값을 직접 적용하면서 시스템 설계가 단순히 코드작성에 그치는 것이 아니라 비즈니스 요구사항과 트래픽 특정까지 종합적으로 고려해야한다는 점도 깨닫게 됐습니다.
 - 더욱 견고한 시스템을 만들기 위해 실제 운영 환경에 대한 이해가 중요하다고 느꼈습니다.
