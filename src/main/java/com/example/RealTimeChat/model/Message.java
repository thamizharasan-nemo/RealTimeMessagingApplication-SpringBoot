package com.example.RealTimeChat.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.persistence.CascadeType;
import lombok.*;
import org.hibernate.annotations.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter @Setter
@ToString(exclude = {"conversation", "sender", "replyTo", "replies"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
@NoArgsConstructor
@FilterDef(name = "deletedMessageFilter", parameters = @ParamDef(name = "isDeleted", type = Boolean.class))
@Filter(name = "deletedMessageFilter", condition = "is_deleted = false")
@EntityListeners(AuditingEntityListener.class)
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private int messageId;

    @Column(columnDefinition = "text")
    private String content;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Conversation conversation;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User sender;

    @CreatedDate
    private Instant createdAt;
    @LastModifiedDate
    private Instant editedAt;

    @ManyToOne
    @JoinColumn(name = "reply_to_id")
    @JsonBackReference("message-replies")
    private Message replyTo;

    @OneToMany(mappedBy = "replyTo",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            fetch = FetchType.LAZY)
    @JsonManagedReference("message-replies")   // marks this as the forward reference
    private Set<Message> replies = new HashSet<>();

    @SQLDelete(sql = "UPDATE Message SET is_deleted = true WHERE message_id = ?")
    private boolean isDeleted = false;
    private String deletedBy;

    private LocalDateTime expiresAt;

    private boolean pinned = false;

    @Enumerated(EnumType.STRING)
    private MessageType messageType;

    public enum MessageType {
        TEXT, IMG, FILE
    }

    public void addReply(Message reply){
        replies.add(reply);
        reply.setReplyTo(this);
    }

    public void removeReply(Message reply){
        replies.remove(reply);
        reply.setReplyTo(null);
    }

}
