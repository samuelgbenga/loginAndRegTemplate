package ng.samuel.regnlogintemplate.entity;

import jakarta.persistence.*;
import lombok.*;
import ng.samuel.regnlogintemplate.enums.TokenType;

@Setter
@Getter
@Entity
@Table(name = "jtoken")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JToken extends BaseClass {
    @Column(unique = true)
    public String token;

    public boolean revoked;

    public boolean expired;

    @Enumerated(EnumType.STRING)
    public TokenType tokenType = TokenType.BEARER;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    public User user;

}