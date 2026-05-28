package com.woodev.saas.common;

import lombok.*;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
//Création d'un classe Page personnalisée nommé PageResponse
//contenant moins d'informations que la classe Page de Spring Data
public class PageResponse<T> {

    private List<T> content;        // Les données
    private int page;               // Page courante
    private int size;               // Taille de page
    private long totalElements;     // Total éléments
    private int totalPages;         // Total pages
    private boolean hasNext;        // Page suivante ?
    private boolean hasPrevious;    // Page précédente ?
    private boolean isFirst;        // Première page ?
    private boolean isLast;         // Dernière page ?

    public static<T> PageResponse<T> of(final Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .isFirst(page.isFirst())
                .isLast(page.isLast())
                .build();
    }

}
