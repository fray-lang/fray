package thosakwe.fray.lang;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import thosakwe.fray.analysis.symbols.FrayTypeMember;
import thosakwe.fray.interpreter.FrayInterpreter;
import thosakwe.fray.interpreter.FrayStackElement;
import thosakwe.fray.interpreter.errors.FrayException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class FrayType extends FrayDatum {
    public static final FrayType OBJECT = new FrayType(null, null, null) {
        @Override
        public String getName() {
            return "Object";
        }
    };

    public static final FrayType TYPE = new FrayType(null, null, OBJECT) {
        @Override
        public String getName() {
            return "Type";
        }
    };

    public static final FrayType VOID = new FrayType(null, null, null) {
        @Override
        public String getName() {
            return "void";
        }
    };
    public static final FrayType FUNCTION = new FrayType(null, null, FrayType.OBJECT) {
        @Override
        public String getName() {
            return "Function";
        }
    };

    private final List<FrayTypeMember> members = new ArrayList<>();

    private Map<String, FrayFunction> constructors = new HashMap<>();

    private FrayType parentType;

    public FrayType(ParseTree source, FrayInterpreter interpreter, FrayType parentType) {
        super(source, interpreter);
        this.parentType = parentType;
    }

    public FrayDatum construct(String constructorName, ParseTree source, List<FrayDatum> args) throws FrayException {
        if (!constructors.containsKey(constructorName)) {
            return getPrototype();
        } else {
            final FrayDatum instance = getPrototype();
            final FrayFunction constructor = constructors.get(constructorName);
            getInterpreter().getSymbolTable().create();
            getInterpreter().getSymbolTable().getInnerMostScope().setThisContext(instance);
            getInterpreter().getStack().push(new FrayStackElement(
                    "constructor invocation",
                    getInterpreter().getSource().getSourcePath(),
                    (ParserRuleContext) source));
            getInterpreter().getStack().push(new FrayStackElement(
                    constructorName.isEmpty() ? "constructor" : constructorName,
                    getInterpreter().getSource().getSourcePath(),
                    (ParserRuleContext) constructor.getSource()));
            constructor.call(getInterpreter(), constructor.getSource(), args);
            getInterpreter().getStack().pop();
            getInterpreter().getStack().pop();
            getInterpreter().getSymbolTable().destroy();
            return instance;
        }
    }

    @Override
    public String curses() {
        return String.format("\033[36m%s", toString());
    }

    public List<FrayTypeMember> getMembers() {
        return members;
    }

    public abstract String getName();

    public FrayType getParentType() {
        return parentType;
    }

    public FrayDatum getPrototype() throws FrayException {
        return null;
    }

    @Override
    public String toString() {
        return String.format("[Type:%s]", getName());
    }

    public Map<String, FrayFunction> getConstructors() {
        return constructors;
    }

    public boolean isAssignableTo(FrayType expectedType) {
        FrayType search = this;

        while (search != null) {
            if (search == expectedType)
                return true;
            search = search.parentType;
        }

        return false;
    }

    public boolean isGeneric() {
        return false;
    }
}
