package com.cs.ss.lib.rabbitmq.rpc.test;
import com.shawn.ss.lib.tools.L;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.util.Set;

/**
 * Created by ss on 2016/7/4.
 */
public class TestScanner {

        public static void main(String[] args) throws Exception {
            System.out.println("Finding annotated classes using Spring:");
            new TestScanner().findAnnotatedClasses("com.cs.ss.lib.rabbitmq.rpc.test");
        }

        public void findAnnotatedClasses(String scanPackage) {
            ClassPathScanningCandidateComponentProvider provider = createComponentScanner();
            Set<BeanDefinition> components = provider.findCandidateComponents(scanPackage);
            L.w("componets:",components.size());
            for (BeanDefinition beanDef :components ) {
                printMetadata(beanDef);
            }
        }

        private ClassPathScanningCandidateComponentProvider createComponentScanner() {
            // Don't pull default filters (@Component, etc.):
            ClassPathScanningCandidateComponentProvider provider
                    = new ClassPathScanningCandidateComponentProvider(false);
            provider.addIncludeFilter(new AnnotationTypeFilter(Findable.class));
            return provider;
        }

        private void printMetadata(BeanDefinition beanDef) {
            try {
                Class<?> cl = Class.forName(beanDef.getBeanClassName());
                Findable findable = cl.getAnnotation(Findable.class);
                if(findable!=null){
                    System.out.println("find s:"+ findable.name());
                }
                System.out.printf("Found class: %s ",
                        cl.getSimpleName());
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Got exception: " + e.getMessage());
            }
        }


}
