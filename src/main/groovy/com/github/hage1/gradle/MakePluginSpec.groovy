package com.github.hage1.gradle

import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import com.google.common.collect.SetMultimap
import groovy.transform.ToString
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Task
import org.gradle.api.logging.Logger
import org.objectweb.asm.ClassReader
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.AnnotationNode
import org.objectweb.asm.tree.ClassNode

import java.nio.file.Path
import java.nio.file.Paths

/**
 * Created by wataru on 13/12/06.
 */
@ToString
class MakePluginSpec implements Action<Task> {
    public static final String PLUGIN_ANNOTATION_ALIAS_FIELD_NAME = 'alias'
    final Set<File> classes
    final Path resourceDir
    final Logger logger
    final Path pluginSpecDir = resourceDir.resolve('META-INF/gradle-plugins')

    MakePluginSpec(Set<File> classes, Path resourceDir, Logger logger) {
        this.classes = classes
        this.resourceDir = resourceDir
        this.logger = logger
    }
    static final def String pluginClassName = Type.getInternalName(Plugin)

    static final def boolean isPlugin(ClassNode cn) {
        cn.interfaces.contains pluginClassName
    }

    final def Map.Entry<String, String> getAlias(File file) {
        assert(file.exists())

        def byte[] buffer = file.getBytes()
        def ClassReader cr = new ClassReader(buffer)
        def ClassNode cn = new ClassNode(Opcodes.ASM4)
        cr.accept(cn, ClassReader.with { SKIP_CODE | SKIP_DEBUG | SKIP_FRAMES })

        if (!isPlugin(cn)) {
            logger.debug("${cn.name} is not plugin")
            return null
        }

        def result = null
        for (ann in cn.visibleAnnotations) {
            def String alias = getAlias(ann as AnnotationNode)
            if (alias) {
                if (result) {
                    logger.error("multiple plugin implementation class spec found in ${file}")
                }
                result = new AbstractMap.SimpleEntry<String,String>(alias, cn.name.replace('/', '.'))
            }
        }
        return result
    }

    static final def String getAlias(AnnotationNode ann) {
        assert ann.values.toArray().length == 2
        if (ann.values[0] == PLUGIN_ANNOTATION_ALIAS_FIELD_NAME) {
            def name = ann.values[1]
            assert name, 'alias undefined'
            assert ! (name as String).empty, 'empty alias name'
            return name
        }
        return null
    }

    final def SetMultimap<String, String> makePluginImplSpec(Collection<File> classes) {
        def SetMultimap<String, String> spec = classes.inject(HashMultimap.create()) {
            Multimap<String, String> acc, File cls ->
                def result = getAlias cls
                if (result) { acc.put result.key, result.value }
                acc
        } as SetMultimap<String, String>
        assert spec.asMap().any{key, value -> ! value.empty }
        spec
    }

    static final def makeContents(String classPath) {
        "implementation-class=${classPath.replace('/','.')}"
    }

    final def File makeSpecFile(String alias) {
        pluginSpecDir.resolve("${alias}.properties").toFile()
    }


    @Override
    void execute(Task task) {
        pluginSpecDir.toFile().with {
            if (! exists()) {
                logger.info("${pluginSpecDir} not exists, so create dirs")
                mkdirs()
            }
        }

        makePluginImplSpec(classes).asMap().each{ String alias, Collection<String> classes ->
            assert classes.toArray().length == 1, "duplication implementations classes of ${alias}: ${classes}"
            makeSpecFile(alias).with {
                def simplePath = task.project.relativeProjectPath((delegate as File).path)
                if (exists()) {
                    logger.debug("create spec file ${simplePath}")
                } else {
                    assert createNewFile()
                }
                logger.info("create spec file ${simplePath} for class ${classes.find()}")
                withWriter { out ->
                    out.writeLine(makeContents(classes.find()))
                }
            }
        }
    }
}
