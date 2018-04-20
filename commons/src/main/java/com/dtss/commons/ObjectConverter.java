package com.dtss.commons;

import java.util.List;

/**
 * Transformer that transforms between dal layer objects and facade layer
 * objects.
 *
 * @param <F> a type from facade layer
 * @param <M> a type from dal layer corresponding to the type from facade
 *            layer.
 */
public interface ObjectConverter<F, M> {
    /**
     * transform the dal layer object to its corresponding facade layer
     * object.
     *
     * @param dto a dal layer object.
     * @return the corresponding facade layer object.
     */
    F toModel(M dto);

    /**
     * transform the facade layer object to its corresponding dal layer
     * object.
     *
     * @param model a facade layer object.
     * @return the corresponding dal layer object.
     */
    M toDto(F model);

    /**
     * transform the list of dal objects to a list of facade objects.
     *
     * @param dtoList a list of dal objects.
     * @return the corresponding list of facade objects.
     */
    List<F> asModelList(List<M> dtoList);

    /**
     * transform the list of facade objects to a list of dal objects.
     *
     * @param modelList a list of facade objects.
     * @return the corresponding list of dal objects.
     */
    List<M> asDtoList(List<F> modelList);
}
