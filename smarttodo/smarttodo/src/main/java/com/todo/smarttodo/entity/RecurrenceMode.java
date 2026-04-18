package com.todo.smarttodo.entity;

public enum RecurrenceMode {
    FIXED,           // always use root deadline
    FROM_COMPLETION  // use actual completed/postponed task
}
