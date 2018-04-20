package com.dtss.commons;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract implementation of {@link ObjectConverter} that return {@code null}
 * for or {@code null} parameter. Implementations of {@link ObjectConverter}
 * can extends this class to avoid redundant {@code null} check.
 *
 * @param <F> facade layer type
 * @param <M> dal layer type
 */
public abstract class AbstractObjectConverter<F, M> implements ObjectConverter<F, M> {

    /**
     * Check whether the specified facade object is null and then use
     * {@link #onBuildDto(Object)}to transform the specified faccade object.
     * <p>
     * {@inheritDoc}
     */
    public M toDto(F model) {
        if (model == null) {
            return null;
        }
        return onBuildDto(model);
    }

    /**
     * It is guaranteed that the specified facade object is a non-null object.
     *
     * @param model a facade layer object
     * @return the corresponding dal layer object
     */
    protected abstract M onBuildDto(F model);

    /**
     * Check whether the specified dal object is null and then use
     * {@link #onBuildModel(Object)} to transform the specified dal object.
     * <p>
     * {@inheritDoc}
     */
    public F toModel(M dto) {
        if (dto == null) {
            return null;
        }
        return onBuildModel(dto);
    }

    protected abstract F onBuildModel(M domain);

    /**
     * {@inheritDoc}
     */
    public List<M> asDtoList(List<F> modelList) {
        if (modelList == null) {
            return null;
        }
        List<M> dtoList = new ArrayList<M>(modelList.size());
        for (F h : modelList) {
            dtoList.add(toDto(h));
        }
        return dtoList;
    }

    /**
     * {@inheritDoc}
     */
    public List<F> asModelList(List<M> dtoList) {
        if (dtoList == null) {
            return null;
        }
        List<F> modelList = new ArrayList<F>(dtoList.size());
        for (M d : dtoList) {
            modelList.add(toModel(d));
        }
        return modelList;
    }

}
