package com.linguacraft.backend.service;

import com.linguacraft.backend.common.BizException;
import com.linguacraft.backend.dto.admin.AdminVocabBookDto;
import com.linguacraft.backend.dto.admin.AdminWordDto;
import com.linguacraft.backend.dto.admin.VocabBookUpsertRequest;
import com.linguacraft.backend.dto.admin.WordUpsertRequest;
import com.linguacraft.backend.entity.VocabBook;
import com.linguacraft.backend.entity.VocabWord;
import com.linguacraft.backend.mapper.FavoriteMapper;
import com.linguacraft.backend.mapper.UserWordProgressMapper;
import com.linguacraft.backend.mapper.UserWordReviewMapper;
import com.linguacraft.backend.mapper.VocabBookMapper;
import com.linguacraft.backend.mapper.VocabWordMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 词书与单词管理服务测试(阶段 A6-①)。
 *
 * <p>覆盖四条业务规则:词书按 filter_level 统计真实词数、声明词数只能显式对齐、
 * 默认词书不可删、单词删除带引用保护;另覆盖新单词 ID 的补零生成。</p>
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdminVocabServiceTest {

    @Mock
    private VocabBookMapper bookMapper;

    @Mock
    private VocabWordMapper wordMapper;

    @Mock
    private UserWordProgressMapper progressMapper;

    @Mock
    private UserWordReviewMapper reviewMapper;

    @Mock
    private FavoriteMapper favoriteMapper;

    private AdminVocabService newService() {
        return new AdminVocabService(bookMapper, wordMapper, progressMapper, reviewMapper, favoriteMapper,
                new ObjectMapper());
    }

    private VocabBook book(String id, String filterLevel, int declaredTotal) {
        VocabBook book = new VocabBook();
        book.setId(id);
        book.setName("词书 " + id);
        book.setFilterLevel(filterLevel);
        book.setTotal(declaredTotal);
        book.setSortOrder(10);
        return book;
    }

    private VocabWord word(String id) {
        VocabWord word = new VocabWord();
        word.setId(id);
        word.setSpelling("abandon");
        word.setPhonetic("/əˈbændən/");
        word.setPartOfSpeech("v.");
        word.setMeanings("[\"放弃;抛弃\"]");
        word.setLevel("core");
        return word;
    }

    private WordUpsertRequest.WordUpsertRequestBuilder validWordRequest() {
        return WordUpsertRequest.builder()
                .spelling("abandon")
                .phonetic("/əˈbændən/")
                .partOfSpeech("v.")
                .meanings(List.of("放弃", "抛弃"))
                .level("core");
    }

    @Test
    @DisplayName("词书列表按 filter_level 统计真实词数,与声明值并列")
    void countsWordsByFilterLevel() {
        when(bookMapper.selectList(any())).thenReturn(List.of(
                book("book-core", "core", 3000), book("book-all", null, 800)));
        when(wordMapper.selectCount(any())).thenReturn(12L, 20L);

        List<AdminVocabBookDto> books = newService().books();

        assertThat(books).hasSize(2);
        assertThat(books.get(0).getActualWordCount()).isEqualTo(12);
        assertThat(books.get(0).getDeclaredTotal()).isEqualTo(3000);
        assertThat(books.get(1).getActualWordCount()).isEqualTo(20);
    }

    @Test
    @DisplayName("声明词数不会被自动改写,只有 sync-total 会对齐")
    void syncTotalIsExplicit() {
        when(bookMapper.selectById("book-core")).thenReturn(book("book-core", "core", 3000));
        when(wordMapper.selectCount(any())).thenReturn(12L);

        AdminVocabBookDto updated = newService().syncBookTotal("book-core");

        assertThat(updated.getDeclaredTotal()).isEqualTo(12);
        ArgumentCaptor<VocabBook> captor = ArgumentCaptor.forClass(VocabBook.class);
        verify(bookMapper).updateById(captor.capture());
        assertThat(captor.getValue().getTotal()).isEqualTo(12);
    }

    @Test
    @DisplayName("词书 filterLevel 只接受三个等级,空串表示不限")
    void validatesBookFilterLevel() {
        assertThatThrownBy(() -> newService().saveBook(null,
                VocabBookUpsertRequest.builder().name("新词书").filterLevel("advanced").build()))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("level 仅接受 high/core/cognitive");

        when(bookMapper.selectList(any())).thenReturn(List.of());
        when(bookMapper.insert(any(VocabBook.class))).thenReturn(1);

        AdminVocabBookDto saved = newService().saveBook(null,
                VocabBookUpsertRequest.builder().name("新词书").filterLevel("  ").total(100).build());

        assertThat(saved.getFilterLevel()).isNull();
    }

    @Test
    @DisplayName("学生端默认词书不许删(前端硬编码 currentBookId)")
    void protectsDefaultBook() {
        when(bookMapper.selectById("book-syllabus")).thenReturn(book("book-syllabus", null, 3000));

        assertThatThrownBy(() -> newService().deleteBook("book-syllabus"))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("学生端默认词书");

        verify(bookMapper, never()).deleteById(any(String.class));
    }

    @Test
    @DisplayName("其它词书可删,且不动单词表(词书只是过滤器)")
    void deletesOtherBook() {
        when(bookMapper.selectById("book-old")).thenReturn(book("book-old", "core", 500));

        newService().deleteBook("book-old");

        verify(bookMapper).deleteById("book-old");
        verify(wordMapper, never()).deleteById(any(String.class));
    }

    @Test
    @DisplayName("单词等级非法即拒绝")
    void rejectsUnknownWordLevel() {
        assertThatThrownBy(() -> newService().saveWord(null, validWordRequest().level("senior").build()))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("level 仅接受 high/core/cognitive");
    }

    @Test
    @DisplayName("释义全为空白即拒绝")
    void rejectsBlankMeanings() {
        assertThatThrownBy(() -> newService().saveWord(null, validWordRequest().meanings(List.of("  ", "")).build()))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("释义 meanings 至少填写一项非空文本");
    }

    @Test
    @DisplayName("拼写重复即拒绝(词表没有拼写唯一键,靠服务层挡)")
    void rejectsDuplicateSpelling() {
        when(wordMapper.selectCount(any())).thenReturn(1L);

        assertThatThrownBy(() -> newService().saveWord(null, validWordRequest().build()))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("拼写已存在");
    }

    @Test
    @DisplayName("新单词 ID 取最大数字后缀 +1 并保持位宽")
    void generatesNextWordIdWithPadding() {
        when(wordMapper.selectCount(any())).thenReturn(0L);
        when(wordMapper.selectList(any())).thenReturn(List.of(word("w-001"), word("w-020")));
        when(wordMapper.insert(any(VocabWord.class))).thenReturn(1);
        when(progressMapper.selectCount(any())).thenReturn(0L);
        when(reviewMapper.selectCount(any())).thenReturn(0L);
        when(favoriteMapper.selectCount(any())).thenReturn(0L);

        AdminWordDto saved = newService().saveWord(null, validWordRequest().build());

        assertThat(saved.getId()).isEqualTo("w-021");
        assertThat(saved.getMeanings()).containsExactly("放弃", "抛弃");
    }

    @Test
    @DisplayName("有学习记录的单词不许删,并说明原因")
    void protectsReferencedWord() {
        when(wordMapper.selectById("w-001")).thenReturn(word("w-001"));
        when(progressMapper.selectCount(any())).thenReturn(3L);
        when(reviewMapper.selectCount(any())).thenReturn(0L);
        when(favoriteMapper.selectCount(any())).thenReturn(1L);

        assertThatThrownBy(() -> newService().deleteWord("w-001"))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("不能删除");

        verify(wordMapper, never()).deleteById(any(String.class));
    }

    @Test
    @DisplayName("无引用的单词可删")
    void deletesUnreferencedWord() {
        when(wordMapper.selectById("w-002")).thenReturn(word("w-002"));
        when(progressMapper.selectCount(any())).thenReturn(0L);
        when(reviewMapper.selectCount(any())).thenReturn(0L);
        when(favoriteMapper.selectCount(any())).thenReturn(0L);

        newService().deleteWord("w-002");

        verify(wordMapper).deleteById("w-002");
    }

    @Test
    @DisplayName("单词不存在时 404")
    void throwsNotFoundForMissingWord() {
        when(wordMapper.selectById("w-999")).thenReturn(null);

        assertThatThrownBy(() -> newService().word("w-999"))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("单词不存在");
    }

    @Test
    @DisplayName("引用统计同时看进度表与复习日志(与学生端「到期数只算存在的词」是同一条纪律)")
    void countsBothProgressAndReviewLogs() {
        when(wordMapper.selectById("w-001")).thenReturn(word("w-001"));
        when(progressMapper.selectCount(any())).thenReturn(0L);
        when(reviewMapper.selectCount(any())).thenReturn(2L);
        when(favoriteMapper.selectCount(any())).thenReturn(0L);

        AdminWordDto dto = newService().word("w-001");

        assertThat(dto.getLearnedUserCount()).isEqualTo(2);
        verify(progressMapper).selectCount(any());
        verify(reviewMapper).selectCount(any());
    }
}
