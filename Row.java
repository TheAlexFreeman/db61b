package db61b;

import java.util.Arrays;
import java.util.List;

/** A single row of a database.
 *  @author Alex Freeman
 */
class Row {
    /** A Row whose column values are DATA.  The array DATA must not be altered
     *  subsequently. */
    Row(String[] data) {
        _data = data;
    }

    /** Given M COLUMNS that were created from a sequence of Tables
     *  [t0,...,tn] as well as ROWS [r0,...,rn] that were drawn from those
     *  same tables [t0,...,tn], constructs a new Row containing M values,
     *  where the ith value of this new Row is taken from the location given
     *  by the ith COLUMN (for each i from 0 to M-1).
     *
     *  More specifically, if _table is the Table number corresponding to
     *  COLUMN i, then the ith value of the newly created Row should come from
     *  ROWS[_table].
     *
     *  Even more specifically, the ith value of the newly created Row should
     *  be equal to ROWS[_table].get(_column), where _column is the column
     *  number in ROWS[_table] corresponding to COLUMN i.
     *
     *  There is a method in the Column class that you'll need to use, see
     *  {@link db61b.Column#getFrom}).  you're wondering why this looks like a
     *  potentially clickable link it is! Just not in source. You might
     *  consider converting this spec to HTML using the Javadoc command.
     */
    Row(List<Column> columns, Row... rows) {
        _data = new String[columns.size()];
        for (int i = 0; i < columns.size(); i += 1) {
            _data[i] = columns.get(i).getFrom(rows);
        }
    }

    /** Return my number of columns. */
    int size() {
        return _data.length;
    }

    /** Return the value of my Kth column.  Requires that 0 <= K < size(). */
    String get(int k) {
        return _data[k];
    }

    @Override
    public boolean equals(Object obj) {
        return this.toString().equals(obj.toString());
    }

    /* NOTE: Whenever you override the .equals() method for a class, you
     * should also override hashCode so as to insure that if
     * two objects are supposed to be equal, they also return the same
     * .hashCode() value (the converse need not be true: unequal objects MAY
     * also return the same .hashCode()).  The hash code is used by certain
     * Java library classes to expedite searches (see Chapter 7 of Data
     * Structures (Into Java)). */

    @Override
    public int hashCode() {
        return Arrays.hashCode(_data);
    }

    /** Returns a String of this row's values, separated by commas. */
    public String toString() {
        String result = "";
        int i;
        for (i = 0; i < _data.length - 1; i += 1) {
            result = result.concat(_data[i] + ",");
        }
        return result.concat(_data[i]);
    }

    /** Contents of this row. */
    private String[] _data;
}
