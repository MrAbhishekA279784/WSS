declare module 'node:sqlite' {
  export interface StatementResultingChanges {
    changes: number | bigint;
    lastInsertRowid: number | bigint;
  }

  export interface StatementSync {
    run(...namedOrPositionalArgs: unknown[]): StatementResultingChanges;
    get(...namedOrPositionalArgs: unknown[]): Record<string, unknown> | undefined;
    all(...namedOrPositionalArgs: unknown[]): Array<Record<string, unknown>>;
  }

  export class DatabaseSync {
    constructor(path: string, options?: { open?: boolean; readOnly?: boolean });
    exec(sql: string): void;
    prepare(sql: string): StatementSync;
    close(): void;
  }
}
