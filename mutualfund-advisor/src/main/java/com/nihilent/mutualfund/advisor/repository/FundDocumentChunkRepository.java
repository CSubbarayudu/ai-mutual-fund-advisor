package com.nihilent.mutualfund.advisor.repository;

import com.nihilent.mutualfund.advisor.entity.FundDocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FundDocumentChunkRepository extends JpaRepository<FundDocumentChunk, Long> {

    List<FundDocumentChunk> findByDocument_DocumentId(Long documentId);

    @Query("SELECT c FROM FundDocumentChunk c WHERE LOWER(c.chunkText) LIKE LOWER(CONCAT('%',:keyword,'%'))")
    List<FundDocumentChunk> findByKeyword(@Param("keyword") String keyword);

    @Query("SELECT c FROM FundDocumentChunk c WHERE c.document.fund.fundId=:fundId AND LOWER(c.chunkText) LIKE LOWER(CONCAT('%',:keyword,'%'))")
    List<FundDocumentChunk> findByFundIdAndKeyword(@Param("fundId") Long fundId, @Param("keyword") String keyword);
}
