import org.simpleframework.xml.*;
import org.simpleframework.xml.convert.Convert;

import java.util.List;

@Root(name = "le", strict = false)
public class Word {

    @ElementList(entry = "f", inline = true)
    public List<WordForm> forms;
}

@Root(name = "f", strict = false)
class WordForm {

    @Attribute(name = "t")
    public String form;

    @Attribute(required = false)
    public Integer frequency;
}
