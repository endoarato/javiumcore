import java.io.DataInputStream;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class JaviumCore2 {

    public static byte readU1(DataInputStream dis) {
        try {
            return dis.readByte();
        } catch (Exception e) {
            printError(e);
        }
        return -1; //TODO placeholder. Is there a better way to do this?
    }

    private static void printError(Exception e) {
        System.err.printf("ERROR: {%s}\n", e);
        System.exit(-1);
    }

    public static short readU2(DataInputStream dis) {
        try {
            return (short)dis.readUnsignedShort();
        } catch (Exception e) {
            printError(e);
        }
        return -1; //TODO placeholder. Is there a better way to do this? 
    }

    public static int readU4(DataInputStream dis) {
        try {
            return dis.readInt();
        } catch (Exception e) {
            printError(e);
        }
        return -1; //TODO placeholder. Is there a better way to do this?
    }

    public abstract class ConstantPoolItem {
        private byte tag;

        private byte getTag() {
            return tag;
        }

        public String toString() {
            return "Tag = " + tag + "\n";
        }
    }

    public class ConstantPoolItemClass extends ConstantPoolItem {
        private short name_index;

        ConstantPoolItemClass(byte tag, short name_index) {
            super.tag = tag;
            this.name_index = name_index;
        }

        private short getNameIndex() {
            return name_index;
        }

        public String toString() {
            return super.toString() + "\n"
                    + "Name Index = " + name_index + "\n";
        }
    }

    public class ConstantPoolItemFieldMethodInterface extends ConstantPoolItem {
        private short class_index;
        private short name_type_index;

        ConstantPoolItemFieldMethodInterface(byte tag, short class_index, short name_type_index) {
            super.tag = tag;
            this.class_index = class_index;
            this.name_type_index = name_type_index;
        }

        private short getClassIndex() {
            return class_index;
        }

        private short getNameTypeIndex() {
            return name_type_index;
        }

        public String toString() {
            return super.toString() + "\n"
                    + "Class Index = " + class_index + "\n"
                    + "Name/Type Index = " + name_type_index + "\n";
        }
    }

    private enum ConstantPoolInfo {
        CONSTANT_CLASS (7),
        CONSTANT_FIELD (9),
        CONSTANT_METHOD (10),
        CONSTANT_INTERFACE_METHOD (11),
        CONSTANT_STRING (8),
        CONSTANT_INTEGER (3),
        CONSTANT_FLOAT (4),
        CONSTANT_LONG (5),
        CONSTANT_DOUBLE (6),
        CONSTANT_NAME_AND_TYPE (12),
        CONSTANT_UTF8 (1),
        CONSTANT_METHOD_HANDLE (15),
        CONSTANT_METHOD_TYPE (16),
        CONSTANT_INVOKE_DYNAMIC (18);

        Integer value;
        ConstantPoolInfo (Integer value) {
            this.value = value;
        }

        private Integer getValue() {
            return this.value;
        }

        // Following 6 lines stolen from https://stackoverflow.com/a/65315398
        private static final Map<Integer, ConstantPoolInfo> lookup =
            Arrays.stream(ConstantPoolInfo.values()).collect(Collectors.toMap(ConstantPoolInfo::getValue, Function.identity()));
        
        public static ConstantPoolInfo fromValue(int value) {
            return lookup.get(value);
        }
    }
    
    public static void main(String[] args) {
        // Open .class file
        String fileName = "./Main.class";
        System.out.println("Parsing Class File: " + fileName);
        try {
            FileInputStream fis = new FileInputStream(fileName);
            DataInputStream dis = new DataInputStream(fis);
            // Read magic (u4)
            String magic = Integer.toHexString(readU4(dis));
            System.out.println("    Magic=" + magic);
            // Read minor version (u2)
            short minor_version = readU2(dis);
            System.out.println("    Minor version=" + minor_version);
            // Read major version (u2)
            short major_version = readU2(dis);
            System.out.println("    Major version=" + major_version);
            // Read constant pool count (u2)
            short constant_pool_count = readU2(dis);
            // Initialize empty constant pool
            ArrayList<ConstantPoolItem> constant_pool = new ArrayList<ConstantPoolItem>();
            System.out.println("    Constant pool count=" + constant_pool_count);
            // Read constant pool
            for (int i = 0; i < constant_pool_count -1; i++) {
                
                // Read tag (u1)
                byte tag = readU1(dis);
                System.out.println("        Tag=" + tag + ", Value: " + ConstantPoolInfo.fromValue(tag));
                // Read info
                    System.out.println("        Info=");
                    // if tag is CONSTANT_Class_info
                    if (tag == ConstantPoolInfo.CONSTANT_CLASS.getValue()) {
                        // Read name index (u2) -> constant_pool
                        short cp_name_index = readU2(dis);
                        System.out.println("            Name Index=" + cp_name_index + ", " + ConstantPoolInfo.fromValue(cp_name_index));
                    }
                    // else if tag is CONSTANT_Fieldref_info, CONSTANT_Methodref_info, CONSTANT InterfaceMethodref_info
                    else if(tag == ConstantPoolInfo.CONSTANT_FIELD.getValue() || tag == ConstantPoolInfo.CONSTANT_METHOD.getValue() || tag == ConstantPoolInfo.CONSTANT_INTERFACE_METHOD.getValue()) {
                    // Read class index (u2) -> constant_pool
                        short cp_class_index = readU2(dis);
                        System.out.println("            Class Index=" + cp_class_index + ", " + ConstantPoolInfo.fromValue(cp_class_index));
                        // Read name and type index (u2) -> constant_pool
                        short cp_name_and_type_index = readU2(dis);
                        System.out.println("            Name and Type Index=" + cp_name_and_type_index + ", " + ConstantPoolInfo.fromValue(cp_name_and_type_index));
                    }
                    // else if tag is CONSTANT_String_info
                    else if(tag == ConstantPoolInfo.CONSTANT_STRING.getValue()) {
                        // Read string_index (u2)  -> constant_pool
                        short cp_string_index = readU2(dis);
                        System.out.println("            String Index=" + cp_string_index + ", " + ConstantPoolInfo.fromValue(cp_string_index));
                    }
                    // else if tag is CONSTANT_Integer_info, CONSTANT_Float_info
                    else if(tag == ConstantPoolInfo.CONSTANT_INTEGER.getValue() || tag == ConstantPoolInfo.CONSTANT_FLOAT.getValue()) {
                        // Read bytes (u4)
                        int cp_int = readU4(dis);
                        System.out.println("            Integer=" + cp_int);
                    }
                    // else if tag is CONSTANT_Long_info, CONSTANT_Double_info
                    else if(tag == ConstantPoolInfo.CONSTANT_LONG.getValue() || tag == ConstantPoolInfo.CONSTANT_DOUBLE.getValue()) {
                        // Read high bytes (u4)
                        int cp_high_bytes = readU4(dis);
                        // Read low bytes (u4)
                        int cp_low_bytes = readU4(dis);
                        long cp_long_double_value = ((long) cp_high_bytes << 32) + cp_low_bytes;
                        System.out.println("            Long/Double Value=" + cp_long_double_value);
                    }
                    // else if tag is CONSTANT_NameAndType_info
                    else if(tag == ConstantPoolInfo.CONSTANT_NAME_AND_TYPE.getValue()) {
                        // Read name index (u2) -> constant_pool
                        short cp_name_index = readU2(dis);
                        System.out.println("            Name Index=" + cp_name_index + ", " + ConstantPoolInfo.fromValue(cp_name_index));
                        // Read descriptor index (u2) -> constant_pool
                        short cp_descriptor_index = readU2(dis);
                        System.out.println("            Descriptor Index=" + cp_descriptor_index + ", " + ConstantPoolInfo.fromValue(cp_descriptor_index));
                    }
                    // else if tag is CONSTANT_Utf8_info
                    else if(tag == ConstantPoolInfo.CONSTANT_UTF8.getValue()) {
                        // Read length (u2)
                        short length = readU2(dis);
                        System.out.println("            Length=" + length);
                        // Read bytes[length] (u1[length])
                        byte[] value = dis.readNBytes(length);
                        String valueString = "";
                        for (int j = 0; j < value.length; j++) {
                            valueString += (char)value[j];
                        }
                        System.out.println("            Value=" + valueString);
                    }
                    // else if tag is CONSTANT_MethodHandle_info
                        // Read reference kind (u1)
                        // Read reference index (u2) -> constant_pool
                    // else if tag is CONSTANT_MethodType_info
                        // Read descriptor index (u2) -> constant_pool
                    // else if tag is CONSTANT_InvokeDynamic_info
                        // Read bootstrap method attr index (u2) -> attributes -> bootstrap_methods
                        // Read name and type index (u2) -> constant_pool
            }
            // Read access flags (u2)
            // Read this class (u2)
            // Read super class (u2)
            // Read interface count (u2)
            // Read interfaces (CONSTANT_Class_info[])
                // Read tag (u1)
                // Read name index (u2) -> constant_pool
            // Read field count (u2)
                // Read access flags (u2)
                // Read name index (u2) -> constant_pool
                // Read descriptor index (u2) -> constant_pool
                // Read attribute count (u2)
                // Read attributes
            // Read fields
            // Read method count (u2)
            // Read methods
            // Read attribute count (u2)
            // Read attributes
                // Read attribute name index (u2) -> constant_pool
                // Read attribute length (u4)
                // Read info
                    // if attribute length suggests attribute is ConstantValue_attribute
                        // info = Read constant value index (u2) -> constant_pool
                    // else if attribute length suggests attribute is Code_attribute
                        //info = 
                            // Read max stack (u2)
                            // Read max locals (u2)
                            // Read code length (u4)
                            // Read code (u1[code length])
                            // Read exception table length (u2)
                            // Read exception table (contents[exception table length])
                                // Read start pc (u2) -> this -> code
                                // Read end pc (u2) -> this -> code
                                // Read handler pc (u2) -> this -> code
                                // Read catch type (u2) -> constant_pool
                            // Read attribute count (u2)
                            // Read attributes
                    // else if attribute length suggests attribute is StackMapTable_attribute
                        // Not going to implement yet
                    // else if attribute length suggests attribute is Exceptions_attribute
                        // Not going to implement yet
                    // else if attribute length suggests attribute is InnerClasses_attribute
                        // Not going to implement yet
                    // else if attribute length suggests attribute is EnclosingMethod_attribute
                        // Not going to implement yet
                    // else if attribute length suggests attribute is Synthetic_attribute
                        // Not going to implement yet
                    // else if attribute length suggests attribute is Signature_attribute
                        // Not going to implement yet
                    // else if attribute length suggests attribute is SourceFile_attribute
                        // Not going to implement yet
                    // else if attribute length suggests attribute is SourceDebugExtension_attribute
                        // Not going to implement yet
                    // else if attribute length suggests attribute is LineNumberTableAttribute
                        // Not going to implement yet
                    // else if attribute length suggests attribute is LocalVariableTable_attribute
                        // Not going to implement yet
                    // else if attribute length suggests attribute is LocalVariableTypeTable_attribute
                        // Not going to implement yet
                    // else if attribute length suggests attribute is Deprecated_attribute
                        // Not going to implement yet
                    // else if attribute length suggests attribute is RuntimeVisibleAnnotations_attribute
                        // Not going to implement yet
                    // else if attribute length suggests attribute is RuntimeInvisibleAnnotations_attribute
                        // Not going to implement yet
                    // else if attribute length suggests attribute is RuntimeVisibleParameterAnnotations_attribute
                        // Not going to implement yet
                    // else if attribute length suggests attribute is RuntimeInvisibleParameterAnnotations_attribute
                        // Not going to implement yet
                    // else if attribute length suggests attribute is AnnotationDefault_attribute
                        // Not going to implement yet
                    // else if attribute length suggests attribute is BootstrapMethods_attribute
                        // Not going to implement yet
        } catch (Exception e) {
            System.err.printf("ERROR: {%s}\n", e);
        }
    }
}
