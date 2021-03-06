package thosakwe.fray.lang;

import org.antlr.v4.runtime.tree.ParseTree;
import thosakwe.fray.interpreter.FrayInterpreter;
import thosakwe.fray.interpreter.errors.FrayException;

import java.util.ArrayList;
import java.util.List;

public class FraySet extends FrayDatum {
    private final List<FrayDatum> items = new ArrayList<>();
    private final FrayIterator iterator;

    public FraySet(ParseTree source, FrayInterpreter interpreter) throws FrayException {
        super(source, interpreter);
        iterator = new FrayIterator(source, interpreter, this);

        registerFinalMember("iterator", iterator);
        registerFinalMember("reverse", new FrayFunction(source, interpreter) {
            @Override
            public FrayDatum call(FrayInterpreter interpreter, ParseTree source, List<FrayDatum> args) throws FrayException {
                final FraySet set = new FraySet(source, interpreter);

                for (int i = items.size() - 1; i >= 0; i--) {
                    set.items.add(items.get(i));
                }

                return set;
            }

            @Override
            public String toString() {
                return "[Set.reverse()]";
            }
        });
    }

    @Override
    public String curses() {
        final StringBuilder builder = new StringBuilder();

        builder.append("\033[0m[");
        int i = 0;

        for (FrayDatum item : items) {
            if (i++ > 0)
                builder.append("\033[0m, ");
            builder.append(item.curses());
        }

        builder.append("\033[0m]");

        return builder.toString();
    }

    public List<FrayDatum> getItems() {
        return items;
    }

    @Override
    public FrayIterator getIterator(FrayInterpreter interpreter) throws FrayException {
        final FrayIterator it = super.getIterator(interpreter);
        return it != null ? it : iterator;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();

        builder.append("[");
        int i = 0;

        for (FrayDatum item : items) {
            if (i++ > 0)
                builder.append(", ");
            builder.append(item);
        }

        builder.append("]");

        return builder.toString();
    }
}
