"""
Example usage of the Analytics Layer.
Demonstrates common workflows and use cases.
"""

from main import AnalyticsOrchestrator


def example_student_analysis():
    """Example: Analyze a single student."""
    print("\n" + "="*80)
    print("EXAMPLE 1: Student Analysis")
    print("="*80)
    
    orchestrator = AnalyticsOrchestrator()
    orchestrator.initialize()
    
    # Analyze student with ID 1
    results = orchestrator.analyze_student(student_id=1, generate_report=True)
    
    print(f"\nStudent: {results['performance']['student_name']}")
    print(f"CGPA: {results['performance']['current_cgpa']}")
    print(f"Performance Category: {results['performance']['performance_category']}")
    print(f"Risk Level: {results['performance']['risk_level']}")
    print(f"Report saved at: {results.get('report_file')}")
    
    orchestrator.shutdown()


def example_teacher_analysis():
    """Example: Analyze a teacher's performance."""
    print("\n" + "="*80)
    print("EXAMPLE 2: Teacher Analysis")
    print("="*80)
    
    orchestrator = AnalyticsOrchestrator()
    orchestrator.initialize()
    
    # Analyze teacher with ID 1
    results = orchestrator.analyze_teacher(teacher_id=1, generate_report=True)
    
    print(f"\nOverall Pass Rate: {results['performance']['overall_pass_rate']}%")
    print(f"Effectiveness Rating: {results['performance']['overall_effectiveness']}")
    print(f"Subjects Taught: {results['performance']['total_subjects']}")
    print(f"Report saved at: {results.get('report_file')}")
    
    orchestrator.shutdown()


def example_subject_comparison():
    """Example: Compare all subjects in a semester."""
    print("\n" + "="*80)
    print("EXAMPLE 3: Subject Comparison")
    print("="*80)
    
    orchestrator = AnalyticsOrchestrator()
    orchestrator.initialize()
    
    # Compare all subjects in semester 1
    results = orchestrator.compare_subjects(semester=1)
    
    print(f"\nTotal Subjects: {results['total_subjects']}")
    print(f"Easiest Subject: {results.get('easiest_subject')}")
    print(f"Hardest Subject: {results.get('hardest_subject')}")
    print(f"Overall Pass Rate: {results['average_overall_pass_rate']}%")
    
    orchestrator.shutdown()


def example_batch_analysis():
    """Example: Batch analyze all students."""
    print("\n" + "="*80)
    print("EXAMPLE 4: Batch Student Analysis")
    print("="*80)
    
    orchestrator = AnalyticsOrchestrator()
    orchestrator.initialize()
    
    # Analyze all students
    results = orchestrator.analyze_all_students(generate_csv=True)
    
    print(f"\nTotal Students Analyzed: {results['total_students']}")
    
    # Show top 5 performers
    top_students = sorted(results['students'], key=lambda x: x['cgpa'], reverse=True)[:5]
    print("\nTop 5 Students by CGPA:")
    for i, student in enumerate(top_students, 1):
        print(f"{i}. {student['name']} (Roll: {student['roll_number']}) - CGPA: {student['cgpa']}")
    
    orchestrator.shutdown()


def example_dashboard():
    """Example: Generate system dashboard."""
    print("\n" + "="*80)
    print("EXAMPLE 5: System Dashboard")
    print("="*80)
    
    orchestrator = AnalyticsOrchestrator()
    orchestrator.initialize()
    
    # Generate dashboard
    dashboard = orchestrator.generate_dashboard_overview()
    
    orchestrator.shutdown()


def example_student_comparison():
    """Example: Compare multiple students."""
    print("\n" + "="*80)
    print("EXAMPLE 6: Student Comparison")
    print("="*80)
    
    orchestrator = AnalyticsOrchestrator()
    orchestrator.initialize()
    
    # Compare students 1, 2, 3, 4, 5
    results = orchestrator.compare_students([1, 2, 3, 4, 5])
    
    print(f"\nStudents Compared: {results['total_students']}")
    print(f"Highest CGPA: {results['highest_cgpa']}")
    print(f"Lowest CGPA: {results['lowest_cgpa']}")
    print(f"Average CGPA: {round(results['average_cgpa'], 2)}")
    
    print("\nRankings:")
    for i, student in enumerate(results['rankings'], 1):
        print(f"{i}. {student['name']} - CGPA: {student['cgpa']}, Backlogs: {student['backlogs']}")
    
    orchestrator.shutdown()


if __name__ == "__main__":
    print("\n" + "="*80)
    print("ANALYTICS LAYER EXAMPLES")
    print("="*80)
    print("\nThese examples demonstrate common analytics workflows.")
    print("Make sure your database is populated with data before running.")
    print("\nNote: Some examples may fail if the corresponding IDs don't exist in the database.")
    print("="*80)
    
    # Uncomment the examples you want to run:
    
    # example_student_analysis()
    # example_teacher_analysis()
    # example_subject_comparison()
    # example_batch_analysis()
    # example_dashboard()
    # example_student_comparison()
    
    print("\n[INFO] Uncomment the examples in this file to run them.")
    print("[INFO] Edit the student/teacher IDs to match your database data.")
