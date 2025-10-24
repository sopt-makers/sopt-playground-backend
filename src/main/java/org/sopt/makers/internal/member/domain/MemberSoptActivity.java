// package org.sopt.makers.internal.member.domain;
//
// import lombok.AccessLevel;
// import lombok.AllArgsConstructor;
// import lombok.Builder;
// import lombok.Getter;
// import lombok.NoArgsConstructor;
//
// import jakarta.persistence.Column;
// import jakarta.persistence.Entity;
// import jakarta.persistence.GeneratedValue;
// import jakarta.persistence.GenerationType;
// import jakarta.persistence.Id;
// import jakarta.persistence.Table;
//
// @Entity
// @Getter
// @NoArgsConstructor(access = AccessLevel.PROTECTED)
// @AllArgsConstructor(access = AccessLevel.PROTECTED)
// @Builder
// @Table(name = "member_sopt_activity")
// // 인증중앙화로 인해 삭제 예정
// public class MemberSoptActivity {
//
//     @Id
//     @GeneratedValue(strategy = GenerationType.IDENTITY)
//     private Long id;
//
//     @Column(name = "user_id")
//     private Long memberId;
//
//     @Column
//     private String part;
//
//     @Column
//     private Integer generation;
//
//     @Column(name = "team")
//     private String team;
//
//     public void setMemberId (Long memberId) {
//         this.memberId = memberId;
//     }
//
//     public void setTeam(String team) {
//         this.team = team;
//     }
// }
