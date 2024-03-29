package db61b;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import static db61b.Utils.*;

/** A single table in a database.
 *  @author P. N. Hilfinger
 */
class Table implements Iterable<Row> {
    /** A new Table whose columns are given by COLUMNTITLES, which may
     *  not contain duplicate names. */
    Table(String[] columnTitles) {
        for (int i = columnTitles.length - 1; i >= 1; i -= 1) {
            for (int j = i - 1; j >= 0; j -= 1) {
                if (columnTitles[i].equals(columnTitles[j])) {
                    throw error("duplicate column name: %s",
                                columnTitles[i]);
                }
            }
        }
        _cols = columnTitles;
    }

    /** A new Table whose columns are give by COLUMNTITLES. */
    Table(List<String> columnTitles) {
        this(columnTitles.toArray(new String[columnTitles.size()]));
    }

    /** Return the number of columns in this table. */
    public int columns() {
        return _cols.length;
    }

    /** Return the title of the Kth column.  Requires 0 <= K < columns(). */
    public String getTitle(int k) {
        return _cols[k];
    }

    /** Return the number of the column whose title is TITLE, or -1 if
     *  there isn't one. */
    public int findColumn(String title) {
        for (int i = 0; i < columns(); i += 1) {
            if (title.equals(getTitle(i))) {
                return i;
            }
        }
        return -1;
    }

    /** Return the number of Rows in this table. */
    public int size() {
        return _rows.size();
    }

    /** Returns an iterator that returns my rows in an unspecfied order. */
    @Override
    public Iterator<Row> iterator() {
        return _rows.iterator();
    }

    /** Add ROW to THIS if no equal row already exists.  Return true if anything
     *  was added, false otherwise. */
    public boolean add(Row row) {
        if (row.size() != columns()) {
            throw error("Improper number of entries in row.");
        }
        return _rows.add(row);
    }

    /** Read the contents of the file NAME.db, and return as a Table.
     *  Format errors in the .db file cause a DBException. */
    static Table readTable(String name) {
        BufferedReader input;
        Table table;
        input = null;
        table = null;
        try {
            input = new BufferedReader(new FileReader(name + ".db"));
            String header = input.readLine();
            if (header == null) {
                throw error("missing header in DB file");
            }
            String[] columnNames = header.split(",");
            table = new Table(columnNames);
            String row;
            while (true) {
                row = input.readLine();
                if (row == null) {
                    break;
                }
                table.add(new Row(row.split(",")));
            }
        } catch (FileNotFoundException e) {
            throw error("could not find %s.db", name);
        } catch (IOException e) {
            throw error("problem reading from %s.db", name);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    /* Ignore IOException */
                }
            }
        }
        return table;
    }

    /** Write the contents of TABLE into the file NAME.db. Any I/O errors
     *  cause a DBException. */
    void writeTable(String name) {
        PrintStream output;
        output = null;
        try {
            String sep;
            sep = "";
            output = new PrintStream(name + ".db");
            int i;
            for (i = 0; i < columns() - 1; i += 1) {
                output.print(getTitle(i));
                output.print(",");
            }
            output.println(getTitle(i));
            for (Row row : this) {
                output.println(row.toString());
            }
        } catch (IOException e) {
            throw error("trouble writing to %s.db", name);
        } finally {
            if (output != null) {
                output.close();
            }
        }
    }

    /** Print my contents on the standard output. */
    void print() {
        for (Row row : _rows) {
            System.out.println("  " + row.toString().replace(",", " "));
        }
    }

    /** Return a new Table whose columns are COLUMNNAMES, selected from
     *  rows of this table that satisfy CONDITIONS. */
    Table select(List<String> columnNames, List<Condition> conditions) {
        Table result = new Table(columnNames);
        ArrayList<Column> cols = new ArrayList<Column>();
        for (String name : columnNames) {
            cols.add(new Column(name, this));
        }
        for (Row row : this) {
            if (Condition.test(conditions, row)) {
                result.add(new Row(cols, row));
            }
        }
        return result;
    }

    /** Return a new Table whose columns are COLUMNNAMES, selected
     *  from pairs of rows from this table and from TABLE2 that match
     *  on all columns with identical names and satisfy CONDITIONS. */
    Table select(Table table2, List<String> columnNames,
                 List<Condition> conditions) {
        if (table2 == null) {
            return this.select(columnNames, conditions);
        }
        Table result = new Table(columnNames);
        ArrayList<Column> joinCols, common1, common2;
        joinCols = new ArrayList<Column>();
        for (String name : columnNames) {
            joinCols.add(new Column(name, this, table2));
        }
        int i;
        HashSet<String> joinTitles = new HashSet<String>();
        for (i = 0; i < this.columns(); i += 1) {
            joinTitles.add(this.getTitle(i));
        }
        common1 = new ArrayList<Column>();
        common2 = new ArrayList<Column>();
        String title;
        for (i = 0; i < table2.columns(); i += 1) {
            title = table2.getTitle(i);
            if (!joinTitles.add(title)) {
                common1.add(new Column(title, this));
                common2.add(new Column(title, table2));
            }
        }
        for (Row row1 : this) {
            for (Row row2 : table2) {
                if (equijoin(common1, common2, row1, row2)
                    && Condition.test(conditions, row1, row2)) {
                    result.add(new Row(joinCols, row1, row2));
                }
            }
        }
        return result;
    }

    /** Return true if the columns COMMON1 from ROW1 and COMMON2 from
     *  ROW2 all have identical values.  Assumes that COMMON1 and
     *  COMMON2 have the same number of elements and the same names,
     *  that the columns in COMMON1 apply to this table, those in
     *  COMMON2 to another, and that ROW1 and ROW2 come, respectively,
     *  from those tables. */
    private static boolean equijoin(List<Column> common1, List<Column> common2,
                                    Row row1, Row row2) {
        for (int i = 0; i < common1.size(); i += 1) {
            String first = common1.get(i).getFrom(row1);
            String second = common2.get(i).getFrom(row2);
            if (!first.equals(second)) {
                return false;
            }
        }
        return true;
    }

    /** My rows. */
    private HashSet<Row> _rows = new HashSet<>();
    /** My column titles. */
    private String[] _cols;
}

