package ph.edu.tsu.tour.core;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class EntityModifiedEvent<E> implements Serializable {

    private static final long serialVersionUID = -228565894594115569L;

    private EntityAction action;
    private E entity;

}